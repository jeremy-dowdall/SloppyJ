/*
 * Copyright (C) 2014 Jeremy Dowdall <jeremyd@aspencloud.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fm.strength.sloppyj;

import static fm.strength.sloppyj.Mapper.CamelSnake.camel;
import static fm.strength.sloppyj.Mapper.CamelSnake.snake;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class MapperTests {
	
	@Test
	public void testCamelCase() throws Exception {
		assertThat(camel(null)).isNull();
		assertThat(camel("key")).isEqualTo("key");
		assertThat(camel("KEY")).isEqualTo("key");
		assertThat(camel("my_key")).isEqualTo("myKey");
		assertThat(camel("MY_KEY")).isEqualTo("myKey");
		assertThat(camel("myKey")).isEqualTo("myKey");
	}

	@Test
	public void testSnakeCase() throws Exception {
		assertThat(snake(null)).isNull();
		assertThat(snake("key")).isEqualTo("key");
		assertThat(snake("Key")).isEqualTo("key");
		assertThat(snake("MyKey")).isEqualTo("my_key");
		assertThat(snake("myKey")).isEqualTo("my_key");
	}

}
