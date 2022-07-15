import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class ZipUtilsTest {
    private static final String TMP_DIRECTORY_SRC = "temp_folder_src_";
    private static final String TMP_DIRECTORY_DEST = "temp_folder_dest_";

    @Test
    void zipFile() throws IOException {
        // 1. Zip file
        File tempDirectorySrc = createTempDirectory(TMP_DIRECTORY_SRC);

        // create source file
        File sourceFile = File.createTempFile("source_file_", ".txt", tempDirectorySrc);

        // add data to source file
        try (OutputStream outputStream = new FileOutputStream(sourceFile)) {
            for (int i = 0; i < 100; i++) {
                String data = "Message" + System.lineSeparator();
                outputStream.write(data.getBytes());
            }
        }

        File zipFile = ZipUtils.zip(sourceFile);
        assertNotNull(zipFile);

        // 2. unZip file
        File tempDirectoryDest = createTempDirectory(TMP_DIRECTORY_DEST);
        assertNotNull(tempDirectoryDest);

        assertTrue(ZipUtils.unZip(zipFile, tempDirectoryDest));

        // 3. validate result
        File[] unZippedFiles = tempDirectoryDest.listFiles();
        assertNotNull(unZippedFiles);
        assertEquals(1, unZippedFiles.length);
        File unZippedFile = unZippedFiles[0];

        assertEquals(sourceFile.length(), unZippedFile.length());

        try (Reader sourceReader = new BufferedReader(new FileReader(sourceFile));
             Reader fileFromCephReader = new BufferedReader(new FileReader(unZippedFile))) {

            assertTrue(IOUtils.contentEquals(sourceReader, fileFromCephReader));
        }
    }

    private File createTempDirectory(String prefix) {
        try {
            var permissions = PosixFilePermissions.fromString("rwxr--r--");
            var attr = PosixFilePermissions.asFileAttribute(permissions);
            return Files.createTempDirectory(prefix, attr).toFile();
        } catch (IOException e) {
            log.error("cannot create temp directory", e);
        }

        return null;
    }
}