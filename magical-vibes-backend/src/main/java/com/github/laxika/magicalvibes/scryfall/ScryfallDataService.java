package com.github.laxika.magicalvibes.scryfall;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ScryfallDataService {

    @Value("${scryfall.cache-dir:./scryfall-cache}")
    private String cacheDir;

    @PostConstruct
    void init() {
        ScryfallOracleLoader.loadAll(cacheDir);
    }
}
