/*
 * Copyright 2018 claudiorosati.
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
package se.europeanspallationsource.xaos.tools.io;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.nio.file.StandardOpenOption.APPEND;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.europeanspallationsource.xaos.tools.io.DirectoryWatcher.create;


/**
 * @author claudio.rosati@esss.se
 */
public class DirectoryWatcherTest {

	private static final Logger LOGGER = Logger.getLogger(DirectoryWatcherTest.class.getName());

	private Path dir_a;
	private Path dir_a_c;
	private Path dir_b;
	private ExecutorService executor;
	private Path file_a;
	private Path file_a_c;
	private Path file_b1;
	private Path file_b2;
	private Path root;

	public DirectoryWatcherTest() {
	}

	@Before
	public void setUp() throws IOException {

		executor = Executors.newSingleThreadExecutor();
		root = Files.createTempDirectory("DW_");
		dir_a = Files.createTempDirectory(root, "DW_a_");
		file_a = Files.createTempFile(dir_a, "DW_a_", ".test");
		dir_a_c = Files.createTempDirectory(dir_a, "DW_a_c_");
		file_a_c = Files.createTempFile(dir_a_c, "DW_a_c_", ".test");
		dir_b = Files.createTempDirectory(root, "DW_b_");
		file_b1 = Files.createTempFile(dir_b, "DW_b1_", ".test");
		file_b2 = Files.createTempFile(dir_b, "DW_b2_", ".test");

//		LOGGER.info(MessageFormat.format(
//			"Testing 'DirectoryWatcher'\n"
//			+ "\tcreated directories:\n"
//			+ "\t\t{0}\n"
//			+ "\t\t{1}\n"
//			+ "\t\t{2}\n"
//			+ "\t\t{3}\n"
//			+ "\tcreated files:\n"
//			+ "\t\t{4}\n"
//			+ "\t\t{5}\n"
//			+ "\t\t{6}\n"
//			+ "\t\t{7}",
//			root,
//			dir_a,
//			dir_a_c,
//			dir_b,
//			file_a,
//			file_a_c,
//			file_b1,
//			file_b2
//		));
//
	}

	@After
	public void tearDown() throws IOException {
		Files.walkFileTree(root, new DeleteFileVisitor());
		executor.shutdown();
	}

	/**
	 * Test of create method, of class DirectoryWatcher.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testCreate() throws IOException {

		LOGGER.info("Testing 'create'…");

		DirectoryWatcher watcher = create(executor);

		assertNotNull(watcher);
		assertFalse(watcher.isShutdown());

		watcher.shutdown();

	}

	/**
	 * Test of getErrorsStream method, of class DirectoryWatcher.
	 */
//	@Test
//	public void testGetErrorsStream() {
//	}

	/**
	 * Test of getSignalledKeysStream method, of class DirectoryWatcher.
	 */
//	@Test
//	public void testGetSignalledKeysStream() {
//	}

	/**
	 * Test of isShutdown method, of class DirectoryWatcher.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testIsShutdown() throws IOException {

		LOGGER.info("Testing 'isShutdown'…");

		DirectoryWatcher watcher = create(executor);

		assertFalse(watcher.isShutdown());

		watcher.shutdown();

		assertTrue(watcher.isShutdown());

	}

	/**
	 * Test of shutdown method, of class DirectoryWatcher.
	 *
	 * @throws java.io.IOException
	 */
	@Test
	public void testShutdown() throws IOException {

		LOGGER.info("Testing 'shutdown'…");

		DirectoryWatcher watcher = create(executor);

		assertFalse(watcher.isShutdown());

//	TODO:CR	Add an operation: it should proceed succesfully.
		watcher.shutdown();

		assertTrue(watcher.isShutdown());

//	TODO:CR	Add an operation: it should throw RejectedExecutionException.
//
	}

	/**
	 * Test of watch method, of class DirectoryWatcher.
	 *
	 * @throws java.lang.Exception
	 */
	@Test
	public void testWatch() throws Exception {

		LOGGER.info("Testing 'watch'…");

		CountDownLatch createLatch = new CountDownLatch(1);
		CountDownLatch deleteLatch = new CountDownLatch(1);
		CountDownLatch modifyLatch = new CountDownLatch(1);
		DirectoryWatcher watcher = create(executor);

		watcher.getSignalledKeysStream().subscribe(key -> {
			key.pollEvents().stream().forEach(e -> {
				if ( StandardWatchEventKinds.ENTRY_CREATE.equals(e.kind()) ) {
					createLatch.countDown();
				} else if ( StandardWatchEventKinds.ENTRY_DELETE.equals(e.kind()) ) {
					deleteLatch.countDown();
				} else if ( StandardWatchEventKinds.ENTRY_MODIFY.equals(e.kind()) ) {
					modifyLatch.countDown();
				}
			});
			key.reset();
		});

		watcher.watch(root);

		Path tmpFile = Files.createTempFile(root, "DW_", ".test");

		if ( !createLatch.await(1, TimeUnit.MINUTES) ) {
			fail("File creation not signalled in 1 minute.");
		}

		Files.write(tmpFile, "Some text content".getBytes(), APPEND);

		if ( !modifyLatch.await(1, TimeUnit.MINUTES) ) {
			fail("File modification not signalled in 1 minute.");
		}

		Files.delete(tmpFile);

		if ( !deleteLatch.await(1, TimeUnit.MINUTES) ) {
			fail("File deletion not signalled in 1 minute.");
		}

		watcher.shutdown();

	}

	/**
	 * Test of watchOrStreamError method, of class DirectoryWatcher.
	 */
//	@Test
//	public void testWatchOrStreamError() {
//	}

}