package com.github.laxika.magicalvibes.carddata.scryfall;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ScryfallDataService {

    private static final Logger LOG = Logger.getLogger(ScryfallDataService.class.getName());

    @Value("${scryfall.cache-dir:./scryfall-cache}")
    private String cacheDir;

    @Value("${oracle.data-provider:SCRYFALL}")
    private OracleDataProvider dataProvider;

    @PostConstruct
    void init() {
        if (dataProvider == OracleDataProvider.MTGJSON) {
            MtgjsonOracleLoader.loadAll(cacheDir);
            return;
        }

        try {
            ScryfallOracleLoader.loadAll(cacheDir);
        } catch (RuntimeException e) {
            LOG.warning("Scryfall oracle load failed (" + e.getMessage() + "); falling back to MTGJSON");
            MtgjsonOracleLoader.loadAll(cacheDir);
        }
    }
}
