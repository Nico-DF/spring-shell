/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.shell.command.CommandParser.CommandParserResults;
import org.springframework.shell.command.CommandParser.MissingOptionException;
import org.springframework.shell.command.CommandParser.NotEnoughArgumentsOptionException;
import org.springframework.shell.command.CommandParser.TooManyArgumentsOptionException;
import org.springframework.shell.command.CommandParser.UnrecognisedOptionException;

import static org.assertj.core.api.Assertions.assertThat;

public class CommandParserTests extends AbstractCommandTests {

	private CommandParser parser;

	@BeforeEach
	public void setupCommandParserTests() {
		ConversionService conversionService = new DefaultConversionService();
		parser = CommandParser.of(conversionService);
	}

	@Test
	public void testEmptyOptionsAndArgs() {
		CommandParserResults results = parser.parse(Collections.emptyList(), new String[0]);
		assertThat(results.results()).hasSize(0);
	}

	@Test
	public void testLongName() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}

	@Test
	public void testShortName() {
		CommandOption option1 = shortOption('a');
		CommandOption option2 = shortOption('b');
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"-a", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
	}

	@Test
	public void testMultipleArgs() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo", "--arg2", "bar"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo");
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(1).value()).isEqualTo("bar");
	}

	@Test
	public void testMultipleArgsWithMultiValues() {
		CommandOption option1 = longOption("arg1", null, false, null, 1, 2);
		CommandOption option2 = longOption("arg2", null, false, null, 1, 2);
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo1", "foo2", "--arg2", "bar1", "bar2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("foo1", "foo2"));
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(1).value()).isEqualTo(Arrays.asList("bar1", "bar2"));
		assertThat(results.positional()).isEmpty();
	}

	@Test
	public void testBooleanWithoutArg() {
		ResolvableType type = ResolvableType.forType(boolean.class);
		CommandOption option1 = shortOption('v', type);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"-v"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(true);
	}

	@Test
	public void testBooleanWithArg() {
		ResolvableType type = ResolvableType.forType(boolean.class);
		CommandOption option1 = shortOption('v', type);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"-v", "false"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(false);
	}

	@Test
	public void testMissingRequiredOption() {
		CommandOption option1 = longOption("arg1", true);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.errors()).hasSize(1);
	}

	@Test
	public void testSpaceInArgWithOneArg() {
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "foo bar"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo bar");
	}

	@Test
	public void testSpaceInArgWithMultipleArgs() {
		CommandOption option1 = longOption("arg1");
		CommandOption option2 = longOption("arg2");
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "foo bar", "--arg2", "hi"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("foo bar");
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(1).value()).isEqualTo("hi");
	}

	@Test
	public void testNonMappedArgs() {
		String[] args = new String[]{"arg1", "arg2"};
		CommandParserResults results = parser.parse(Collections.emptyList(), args);
		assertThat(results.results()).hasSize(0);
		assertThat(results.positional()).containsExactly("arg1", "arg2");
	}

	@Test
	public void testNonMappedArgBeforeOption() {
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"foo", "--arg1", "value"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("value");
		assertThat(results.positional()).containsExactly("foo");
	}

	@Test
	public void testNonMappedArgAfterOption() {
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "value", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("value");
		assertThat(results.positional()).containsExactly("foo");
	}

	@Test
	public void testNonMappedArgWithoutOption() {
		CommandOption option1 = longOption("arg1", 0, 1, 2);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"value", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		// no type so we get raw list
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("value", "foo"));
		assertThat(results.positional()).containsExactly("value", "foo");
	}

	@Test
	public void testNonMappedArgWithoutOptionHavingType() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(String.class), false, 0, 1, 2);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"value", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("value,foo");
		assertThat(results.positional()).containsExactly("value", "foo");
	}

	@Test
	public void testMappedFromArgToString() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(String.class), false, 0, 1, 2);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "value", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo("value,foo");
		assertThat(results.positional()).isEmpty();
	}

	@Test
	public void testShortOptionsCombined() {
		CommandOption optionA = shortOption('a');
		CommandOption optionB = shortOption('b');
		CommandOption optionC = shortOption('c');
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc"};

		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isNull();
		assertThat(results.results().get(1).value()).isNull();
		assertThat(results.results().get(2).value()).isNull();
	}

	@Test
	public void testShortOptionsCombinedBooleanType() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc"};

		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isEqualTo(true);
		assertThat(results.results().get(1).value()).isEqualTo(true);
		assertThat(results.results().get(2).value()).isEqualTo(true);
	}

	@Test
	public void testShortOptionsCombinedBooleanTypeArgFalse() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-abc", "false"};

		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionB);
		assertThat(results.results().get(2).option()).isSameAs(optionC);
		assertThat(results.results().get(0).value()).isEqualTo(false);
		assertThat(results.results().get(1).value()).isEqualTo(false);
		assertThat(results.results().get(2).value()).isEqualTo(false);
	}

	@Test
	public void testShortOptionsCombinedBooleanTypeSomeArgFalse() {
		CommandOption optionA = shortOption('a', ResolvableType.forType(boolean.class));
		CommandOption optionB = shortOption('b', ResolvableType.forType(boolean.class));
		CommandOption optionC = shortOption('c', ResolvableType.forType(boolean.class));
		List<CommandOption> options = Arrays.asList(optionA, optionB, optionC);
		String[] args = new String[]{"-ac", "-b", "false"};

		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(3);
		assertThat(results.results().get(0).option()).isSameAs(optionA);
		assertThat(results.results().get(1).option()).isSameAs(optionC);
		assertThat(results.results().get(2).option()).isSameAs(optionB);
		assertThat(results.results().get(0).value()).isEqualTo(true);
		assertThat(results.results().get(1).value()).isEqualTo(true);
		assertThat(results.results().get(2).value()).isEqualTo(false);
	}

	@Test
	public void testLongOptionsWithArray() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(int[].class));
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(new int[] { 1, 2 });
	}

	@Test
	public void testLongOptionsWithStringArray() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(String[].class));
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(new String[] { "1", "2" });
	}

	@Test
	public void testLongOptionsWithPlainList() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(List.class));
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("1", "2"));
	}

	@Test
	public void testLongOptionsWithTypedList() {
		CommandOption option1 = longOption("arg1", ResolvableType.forClassWithGenerics(List.class, String.class));
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("1", "2"));
	}

	@Test
	public void testArityErrors() {
		CommandOption option1 = CommandOption.of(
				new String[] { "arg1" },
				null,
				null,
				null,
				ResolvableType.forType(int[].class),
				true,
				null,
				null,
				2,
				3,
				null,
				null);

		List<CommandOption> options = Arrays.asList(option1);

		String[] args1 = new String[]{"--arg1", "1", "2", "3", "4"};
		CommandParserResults results1 = parser.parse(options, args1);
		assertThat(results1.errors()).hasSize(1);
		assertThat(results1.errors().get(0)).isInstanceOf(TooManyArgumentsOptionException.class);
		assertThat(results1.results()).hasSize(1);
		assertThat(results1.results().get(0).option()).isSameAs(option1);
		assertThat(results1.results().get(0).value()).isNull();

		String[] args2 = new String[]{"--arg1", "1"};
		CommandParserResults results2 = parser.parse(options, args2);
		assertThat(results2.errors()).hasSize(1);
		assertThat(results2.errors().get(0)).isInstanceOf(NotEnoughArgumentsOptionException.class);
		assertThat(results2.results()).hasSize(1);
		assertThat(results2.results().get(0).option()).isSameAs(option1);
		assertThat(results2.results().get(0).value()).isNull();
	}

	@Test
	public void testMapToIntArray() {
		CommandOption option1 = CommandOption.of(
				new String[] { "arg1" },
				null,
				null,
				null,
				ResolvableType.forType(int[].class),
				false,
				null,
				0,
				1,
				2,
				null,
				null);


		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.errors()).hasSize(0);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(new int[] { 1, 2 });
	}

	@Test
	public void testMapPositionalArgs1() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(String.class), false, 0, 1, 1);
		CommandOption option2 = longOption("arg2", ResolvableType.forType(String.class), false, 1, 1, 2);
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(0).value()).isEqualTo("1");
		assertThat(results.results().get(1).value()).isEqualTo("2");
	}

	@Test
	public void testMapPositionalArgsNoTypeDefined() {
		CommandOption option1 = longOption("arg1", 0, 1, 1);
		CommandOption option2 = longOption("arg2", 1, 1, 2);
		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"--arg1", "1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("1"));
		// no type so we get raw list
		assertThat(results.results().get(1).value()).isEqualTo("2");
	}

	@Test
	public void testMapPositionalArgsWhenTypeList() {
		CommandOption option1 = CommandOption.of(
				new String[] { "arg1" },
				null,
				null,
				null,
				ResolvableType.forType(List.class),
				true,
				null,
				0,
				1,
				1,
				null,
				null);

		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"1"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(Arrays.asList("1"));
	}

	@Test
	public void testMapPositionalArgs2() {
		CommandOption option1 = longOption("arg1", ResolvableType.forType(String.class), false, 0, 1, 1);
		CommandOption option2 = longOption("arg2", ResolvableType.forType(String.class), false, 1, 1, 2);

		List<CommandOption> options = Arrays.asList(option1, option2);
		String[] args = new String[]{"1", "2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(2);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(1).option()).isSameAs(option2);
		assertThat(results.results().get(0).value()).isEqualTo("1");
		assertThat(results.results().get(1).value()).isEqualTo("2");
	}

	@Test
	public void testBooleanWithDefault() {
		ResolvableType type = ResolvableType.forType(boolean.class);
		CommandOption option1 = CommandOption.of(new String[] { "arg1" }, null, new Character[0], "description", type, false,
				"true", null, null, null, null, null);

		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(true);
	}

	@Test
	public void testIntegerWithDefault() {
		ResolvableType type = ResolvableType.forType(Integer.class);
		CommandOption option1 = CommandOption.of(new String[] { "arg1" }, null, new Character[0], "description", type, false,
				"1", null, null, null, null, null);

		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(1);
	}

	@Test
	public void testIntegerWithGivenValue() {
		ResolvableType type = ResolvableType.forType(Integer.class);
		CommandOption option1 = CommandOption.of(new String[] { "arg1" }, null, new Character[0], "description", type, false,
				null, null, null, null, null, null);

		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[] { "--arg1", "1" };
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.results().get(0).option()).isSameAs(option1);
		assertThat(results.results().get(0).value()).isEqualTo(1);
	}

	@Test
	public void testNotDefinedLongOptionWithoutOptions() {
		// gh-602
		List<CommandOption> options = Arrays.asList();
		String[] args = new String[]{"--arg1", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(0);
		assertThat(results.errors()).hasSize(1);
		assertThat(results.errors()).satisfiesExactly(
			e -> {
				assertThat(e).isInstanceOf(UnrecognisedOptionException.class);
			}
		);
		assertThat(results.positional()).hasSize(1);
	}

	@Test
	public void testNotDefinedLongOptionWithOptionalOption() {
		// gh-602
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "bar", "--arg2", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.errors()).hasSize(1);
		assertThat(results.errors()).satisfiesExactly(
			e -> {
				assertThat(e).isInstanceOf(UnrecognisedOptionException.class);
			}
		);
		assertThat(results.positional()).hasSize(1);
	}

	@Test
	public void testNotDefinedLongOptionWithRequiredOption() {
		// gh-602
		CommandOption option1 = longOption("arg1", true);
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg2", "foo"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(0);
		assertThat(results.errors()).hasSize(2);
		assertThat(results.errors()).satisfiesExactly(
			e -> {
				assertThat(e).isInstanceOf(UnrecognisedOptionException.class);
			},
			e -> {
				assertThat(e).isInstanceOf(MissingOptionException.class);
			}
		);
		assertThat(results.positional()).hasSize(1);
	}

	@Test
	public void testDashOptionValueDoNotError() {
		// gh-651
		CommandOption option1 = longOption("arg1");
		List<CommandOption> options = Arrays.asList(option1);
		String[] args = new String[]{"--arg1", "-1"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(1);
		assertThat(results.errors()).hasSize(0);
		assertThat(results.positional()).hasSize(0);
	}

	@Test
	public void testPositionDoesNotAffectRequiredErrorWithOtherErrors() {
		// gh-601
		CommandOption o1 = CommandOption.of(
				new String[] { "arg1" },
				null,
				null,
				null,
				null,
				true,
				null,
				0,
				1,
				1,
				null,
				null);

		List<CommandOption> options = Arrays.asList(o1);
		String[] args = new String[]{"--arg2"};
		CommandParserResults results = parser.parse(options, args);
		assertThat(results.results()).hasSize(0);
		assertThat(results.errors()).hasSize(2);
		assertThat(results.positional()).hasSize(0);
	}

	private static CommandOption longOption(String name) {
		return longOption(name, null);
	}

	private static CommandOption longOption(String name, boolean required) {
		return longOption(name, null, required, null);
	}

	private static CommandOption longOption(String name, ResolvableType type) {
		return longOption(name, type, false, null);
	}

	private static CommandOption longOption(String name, int position, int arityMin, int arityMax) {
		return longOption(name, null, false, position, arityMin, arityMax);
	}

	private static CommandOption longOption(String name, ResolvableType type, boolean required, Integer position) {
		return longOption(name, type, required, position, null, null);
	}

	private static CommandOption longOption(String name, ResolvableType type, boolean required, Integer position, Integer arityMin, Integer arityMax) {
		return CommandOption.of(new String[] { name }, null, new Character[0], "desc", type, required, null, position,
				arityMin, arityMax, null, null);
	}

	private static CommandOption shortOption(char name) {
		return shortOption(name, null);
	}

	private static CommandOption shortOption(char name, ResolvableType type) {
		return CommandOption.of(new String[0], new Character[] { name }, "desc", type);
	}
}
