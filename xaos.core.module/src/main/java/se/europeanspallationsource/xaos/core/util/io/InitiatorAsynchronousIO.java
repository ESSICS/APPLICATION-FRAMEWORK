/*
 * Copyright 2018 European Spallation Source ERIC.
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
package se.europeanspallationsource.xaos.core.util.io;


import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.CompletionStage;


/**
 * API for asynchronous file-system operations similar to
 * {@link AsynchronousIO}, where each "file-system changing" operation
 * takes an extra argument: the <i>initiator</i> of the change.
 *
 * @param <I> Type of the <i>initiator</i> of the I/O operation.
 * @author claudio.rosati@esss.se
 * @see <a href="https://github.com/TomasMikula/LiveDirsFX">LiveDirsFX:org.fxmisc.livedirs.InitiatorTrackingIOFacility</a>
 */
public interface InitiatorAsynchronousIO<I> {

	/**
	 * Create a new directory by creating all nonexistent parent directories
	 * first. If an I/O error occurs, the returned completion stage is completed
	 * exceptionally with the encountered error.
	 *
	 * @param dir       The pathname of the file to be created.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the directory.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case of an
	 *         exception is thrown.
	 */
	CompletionStage<Void> createDirectories( Path dir, I initiator, FileAttribute<?>... attrs );

	/**
	 * Creates a directory. If the directory already exists, or its parent
	 * directory does not exist, or another I/O error occurs, the returned
	 * completion stage is completed exceptionally with the encountered error.
	 *
	 * @param dir       The pathname of the directory to be created.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the directory.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case the
	 *         directory already exists, or its parent directory does not exist,
	 *         or an exception is thrown.
	 */
	CompletionStage<Void> createDirectory( Path dir, I initiator, FileAttribute<?>... attrs );

	/**
	 * Creates an empty file. If file already exists or an I/O error occurs,
	 * the returned completion stage is completed exceptionally with the
	 * encountered error.
	 *
	 * @param file      The pathname of the file to be created.
	 * @param initiator The initiator of the operation.
	 * @param attrs     An optional list of file attributes to set atomically
	 *                  when creating the directory.
	 * @return An exceptionally completed {@link CompletionStage} in case the
	 *         file already exists, or an exception is thrown.
	 */
	CompletionStage<Void> createFile( Path file, I initiator, FileAttribute<?>... attrs );

	/**
	 * Deletes a file or an empty directory. If an I/O error occurs, the returned
	 * completion stage is completed exceptionally with the encountered error.
	 *
	 * @param path      Pathname of the file or directory to be deleted.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case an
	 *         exception is thrown.
	 */
	CompletionStage<Void> delete( Path path, I initiator );

	/**
	 * Deletes a file tree rooted at the given path. If an I/O error occurs,
	 * the returned completion stage is completed exceptionally with the
	 * encountered error.
	 *
	 * @param root      The path to the file tree root to be deleted.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case an
	 *         exception is thrown.
	 */
	CompletionStage<Void> deleteTree( Path root, I initiator );

	/**
	 * Reads the contents of a binary file. The returned completion stage will
	 * contain the read content as a byte array or, if an I/O error occurs, it
	 * will be completed exceptionally with the encountered error.
	 *
	 * @param file      The pathname of the file to be read.
	 * @param initiator The initiator of the operation.
	 * @return A {@link CompletionStage} containing the read content as a byte
	 *         array or, if an I/O error occurs, the encountered error.
	 */
	CompletionStage<byte[]> readBinaryFile( Path file, I initiator );

	/**
	 * Reads the contents of a text file. The returned completion stage will
	 * contain the read content as a string or, if an I/O error occurs, it will
	 * be completed exceptionally with the encountered error.
	 *
	 * @param file      The pathname of the file to be read.
	 * @param charset   The {@link Charset} used.
	 * @param initiator The initiator of the operation.
	 * @return A {@link CompletionStage} containing the read content as a string
	 *         or, if an I/O error occurs, the encountered error.
	 */
	CompletionStage<String> readTextFile( Path file, Charset charset, I initiator );

	/**
	 * Reads the contents of an UTF8-encoded file. The returned completion stage
	 * will contain the read content as a string or, if an I/O error occurs, it
	 * will be completed exceptionally with the encountered error.
	 *
	 * @param file      The pathname of the file to be read.
	 * @param initiator The initiator of the operation.
	 * @return A {@link CompletionStage} containing the read content as a string
	 *         or, if an I/O error occurs, the encountered error.
	 */
	default CompletionStage<String> readUTF8File( Path file, I initiator ) {
		return readTextFile(file, Charset.forName("UTF-8"), initiator);
	}

	/**
	 * Returns an {@link AsynchronousIO} that delegates all operations
	 * to this facility with the given {@code initiator} of changes.
	 *
	 * @param initiator The initiator of the operation.
	 * @return An {@link AsynchronousIO} that delegates all operations
	 *         to this facility with the given {@code initiator} of changes.
	 */
	default AsynchronousIO withInitiator( final I initiator ) {
		return new AsynchronousIO() {

			@Override
			public CompletionStage<Void> createDirectories( Path dir, FileAttribute<?>... attrs ) {
				return InitiatorAsynchronousIO.this.createDirectories(dir, initiator, attrs);
			}

			@Override
			public CompletionStage<Void> createDirectory( Path dir, FileAttribute<?>... attrs ) {
				return InitiatorAsynchronousIO.this.createDirectory(dir, initiator, attrs);
			}

			@Override
			public CompletionStage<Void> createFile( Path file, FileAttribute<?>... attrs ) {
				return InitiatorAsynchronousIO.this.createFile(file, initiator);
			}

			@Override
			public CompletionStage<Void> delete( Path fileOrDir ) {
				return InitiatorAsynchronousIO.this.delete(fileOrDir, initiator);
			}

			@Override
			public CompletionStage<Void> deleteTree( Path root ) {
				return InitiatorAsynchronousIO.this.deleteTree(root, initiator);
			}

			@Override
			public CompletionStage<byte[]> readBinaryFile( Path file ) {
				return InitiatorAsynchronousIO.this.readBinaryFile(file, initiator);
			}

			@Override
			public CompletionStage<String> readTextFile( Path file, Charset charset ) {
				return InitiatorAsynchronousIO.this.readTextFile(file, charset, initiator);
			}

			@Override
			public CompletionStage<Void> writeBinaryFile( Path file, byte[] content ) {
				return InitiatorAsynchronousIO.this.writeBinaryFile(file, content, initiator);
			}

			@Override
			public CompletionStage<Void> writeTextFile( Path file, String content, Charset charset ) {
				return InitiatorAsynchronousIO.this.writeTextFile(file, content, charset, initiator);
			}

		};
	}

	/**
	 * Writes binary file to disk. If an I/O error occurs, the returned
	 * completion stage is completed exceptionally with the encountered error.
	 *
	 * @param file      The pathname of the file to be created.
	 * @param content   The bytes to be written into the file.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case an
	 *         exception is thrown.
	 */
	CompletionStage<Void> writeBinaryFile( Path file, byte[] content, I initiator );

	/**
	 * Writes the given string in a text file to disk. If an I/O error occurs,
	 * the returned completion stage is completed exceptionally with the
	 * encountered error.
	 *
	 * @param file      The pathname of the file to be created.
	 * @param content   The String to be written in the file.
	 * @param charset   The {@link Charset} used.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case an
	 *         exception is thrown.
	 */
	CompletionStage<Void> writeTextFile( Path file, String content, Charset charset, I initiator );

	/**
	 * Writes UTF8-encoded text to disk. If an I/O error occurs, the returned
	 * completion stage is completed exceptionally with the encountered error.
	 *
	 * @param file      The pathname of the file to be created.
	 * @param content   The String to be written into the file.
	 * @param initiator The initiator of the operation.
	 * @return An exceptionally completed {@link CompletionStage} in case an
	 *         exception is thrown.
	 */
	default CompletionStage<Void> writeUTF8File( Path file, String content, I initiator ) {
		return writeTextFile(file, content, Charset.forName("UTF-8"), initiator);
	}

}
