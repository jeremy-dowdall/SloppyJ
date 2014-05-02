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

import static fm.strength.sloppyj.SnakeMapper.fromSnake;
import static fm.strength.sloppyj.SnakeMapper.toSnake;
import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;

public class MapperTests {
	
	@Test
	public void test_fromSnake() throws Exception {
		assertThat(fromSnake(null)).isNull();
		assertThat(fromSnake("key")).isEqualTo("key");
		assertThat(fromSnake("KEY")).isEqualTo("key");
		assertThat(fromSnake("my_key")).isEqualTo("myKey");
		assertThat(fromSnake("MY_KEY")).isEqualTo("myKey");
		assertThat(fromSnake("my_key_a_b_c")).isEqualTo("myKeyABC");
		assertThat(fromSnake("my_key_200")).isEqualTo("myKey200");
		assertThat(fromSnake("myKey")).isEqualTo("myKey");
		assertThat(fromSnake("myKey200")).isEqualTo("myKey200");
	}

	@Test
	public void test_toSnake() throws Exception {
		assertThat(toSnake(null)).isNull();
		assertThat(toSnake("key")).isEqualTo("key");
		assertThat(toSnake("Key")).isEqualTo("key");
		assertThat(toSnake("MyKey")).isEqualTo("my_key");
		assertThat(toSnake("myKey")).isEqualTo("my_key");
		assertThat(toSnake("myKeyABC")).isEqualTo("my_key_a_b_c");
		assertThat(toSnake("myKey200")).isEqualTo("my_key_200");
	}

}
