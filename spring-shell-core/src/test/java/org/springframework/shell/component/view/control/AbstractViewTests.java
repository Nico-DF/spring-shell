/*
 * Copyright 2023 the original author or authors.
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
package org.springframework.shell.component.view.control;

import org.assertj.core.api.AssertProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.shell.component.view.ScreenAssert;
import org.springframework.shell.component.view.event.DefaultEventLoop;
import org.springframework.shell.component.view.event.KeyEvent;
import org.springframework.shell.component.view.event.KeyHandler;
import org.springframework.shell.component.view.event.KeyHandler.KeyHandlerResult;
import org.springframework.shell.component.view.event.MouseEvent;
import org.springframework.shell.component.view.event.MouseHandler;
import org.springframework.shell.component.view.event.MouseHandler.MouseHandlerResult;
import org.springframework.shell.component.view.screen.DefaultScreen;
import org.springframework.shell.component.view.screen.Screen;

public class AbstractViewTests {

	protected Screen screen24x80;
	protected Screen screen7x10;
	protected Screen screen10x10;
	protected Screen screen0x0;
	protected DefaultEventLoop eventLoop;

	@BeforeEach
	void setup() {
		screen24x80 = new DefaultScreen(24, 80);
		screen7x10 = new DefaultScreen(7, 10);
		screen0x0 = new DefaultScreen();
		screen10x10 = new DefaultScreen(10, 10);
		eventLoop = new DefaultEventLoop();
	}

	@AfterEach
	void cleanup() {
		if (eventLoop != null) {
			eventLoop.destroy();
		}
		eventLoop = null;
	}

	protected void configure(View view) {
		if (eventLoop != null) {
			if (view instanceof AbstractView v) {
				v.setEventLoop(eventLoop);
			}
			eventLoop.onDestroy(eventLoop.mouseEvents()
				.doOnNext(m -> {
					view.getMouseHandler().handle(MouseHandler.argsOf(m));
				})
				.subscribe());
			eventLoop.onDestroy(eventLoop.keyEvents()
				.doOnNext(m -> {
					view.getKeyHandler().handle(KeyHandler.argsOf(m));
				})
				.subscribe());
		}
	}

	protected void dispatchEvent(View view, KeyEvent event) {
		KeyHandler keyHandler = view.getKeyHandler();
		if (keyHandler != null) {
			keyHandler.handle(new KeyHandler.KeyHandlerArgs(event));
		}
	}

	protected AssertProvider<ScreenAssert> forScreen(Screen screen) {
		return () -> new ScreenAssert(screen);
	}

	protected KeyHandlerResult handleKey(View view, Integer key) {
		return handleKeyEvent(view, KeyEvent.of(key));
	}

	protected KeyHandlerResult handleKeyEvent(View view, KeyEvent key) {
		return view.getKeyHandler().handle(KeyHandler.argsOf(key));
	}

	protected MouseEvent mouseClick(int x, int y) {
		return MouseEvent.of(x, y, MouseEvent.Type.Released | MouseEvent.Button.Button1);
	}

	protected MouseHandlerResult handleMouseClick(View view, int x, int y) {
		MouseEvent click = mouseClick(0, 2);
		return handleMouseClick(view, click);
	}

	protected MouseHandlerResult handleMouseClick(View view, MouseEvent click) {
		return view.getMouseHandler().handle(MouseHandler.argsOf(click));
	}

	protected MouseHandlerResult handleMouseWheelDown(View view, int x, int y) {
		MouseEvent wheel = mouseWheelDown(x, y);
		return view.getMouseHandler().handle(MouseHandler.argsOf(wheel));
	}

	protected MouseHandlerResult handleMouseWheelUp(View view, int x, int y) {
		MouseEvent wheel = mouseWheelUp(x, y);
		return view.getMouseHandler().handle(MouseHandler.argsOf(wheel));
	}

	protected MouseEvent mouseWheelUp(int x, int y) {
		return MouseEvent.of(x, y, MouseEvent.Type.Wheel | MouseEvent.Button.WheelUp);
	}

	protected MouseEvent mouseWheelDown(int x, int y) {
		return MouseEvent.of(x, y, MouseEvent.Type.Wheel | MouseEvent.Button.WheelDown);
	}
}
