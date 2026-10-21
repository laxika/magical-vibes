package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Downloads one set from an upstream provider and converts it into a {@link StoredSet}.
 *
 * <p>Everything provider-specific stops here: HTTP, field naming, which JSON node a face lives on,
 * array ordering and syntax quirks. Rules decisions about a face do not belong here — they live in
 * {@link com.github.laxika.magicalvibes.carddata.FaceOracleMapper}, which runs on load.
 */
public interface SetSource {

    OracleDataProvider provider();

    /**
     * @param setCode the set to download, e.g. {@code "ISD"}
     * @param tempDir a fresh directory for the raw download; {@link SetDataStore} deletes it
     *                afterwards, so a source never cleans up after itself
     * @throws IOException if the provider is unreachable or returns something unusable; the store
     *                     retries and then falls back to the other provider
     */
    StoredSet download(String setCode, Path tempDir) throws IOException, InterruptedException;
}
