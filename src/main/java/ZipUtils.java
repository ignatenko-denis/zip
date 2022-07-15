import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public class ZipUtils {
    private static final byte[] EMPTY = new byte[0];

    public static File zip(File sourceFile) {
        if (sourceFile == null || !sourceFile.exists()) {
            log.error("Source file '{}' doesn't exist", sourceFile);
            return null;
        }

        String baseName = FilenameUtils.getBaseName(sourceFile.getName());
        File zipFile = new File(sourceFile.getParentFile(), baseName + ".zip");

        try (FileInputStream in = new FileInputStream(sourceFile);
             ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
            zipOut.putNextEntry(zipEntry);

            IOUtils.copy(in, zipOut);
        } catch (Exception e) {
            log.error(String.format("error on Zip '%s'", sourceFile), e);
            return null;
        }

        return zipFile;
    }

    public static boolean unZip(File zipFile, File destinationDirectory) {
        if (zipFile == null || !zipFile.exists()) {
            log.error("Zip file '{}' doesn't exist", zipFile);
            return false;
        }

        if (destinationDirectory == null || !destinationDirectory.isDirectory()) {
            log.error("Destination directory '{}' doesn't exist", destinationDirectory);
            return false;
        }

        destinationDirectory.mkdir();

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile.getAbsolutePath()))) {
            ZipEntry zipEntry = zipIn.getNextEntry();

            while (zipEntry != null) {
                File newFile = new File(destinationDirectory, zipEntry.getName());

                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    IOUtils.copy(zipIn, out);
                }

                zipEntry = zipIn.getNextEntry();
            }

            zipIn.closeEntry();
        } catch (Exception e) {
            String message = String.format("error on unZip '%s' to directory '%s'",
                    zipFile, destinationDirectory);
            log.error(message, e);
            return false;
        }

        return true;
    }

    public static byte[] unZip(InputStream zipStream) {
        try (ZipInputStream zipIn = new ZipInputStream(zipStream)) {
            ZipEntry zipEntry = zipIn.getNextEntry();

            if (zipEntry != null) {
                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    IOUtils.copy(zipIn, out);
                    return out.toByteArray();
                }
            }
        } catch (Exception e) {
            log.error("error on unZip stream", e);
            return EMPTY;
        }

        return EMPTY;
    }
}
