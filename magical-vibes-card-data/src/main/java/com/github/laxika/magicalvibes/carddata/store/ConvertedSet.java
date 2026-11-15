package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;

import java.time.Instant;
import java.util.List;

/**
 * One downloaded set, converted: what a {@link SetSource} hands the {@link SetDataStore}, which
 * writes each part to its own file.
 *
 * @param set     what loading the set reads
 * @param details what only the card browser and deck validation read
 */
public record ConvertedSet(StoredSet set, StoredSetDetails details) {

    /**
     * @param sourceDate   the provider's own build date (MTGJSON {@code meta.date}), or null
     * @param tokenSetCode the Scryfall token set code the token printings' images are served under
     * @param printings    every printing in the set, implemented or not
     * @param tokens       every token printing in the set's token list, emblems included
     */
    public static ConvertedSet of(String setCode, String setName, OracleDataProvider source,
                                  String sourceDate, Instant fetchedAt, String tokenSetCode,
                                  List<ConvertedPrinting> printings, List<StoredToken> tokens) {
        return new ConvertedSet(
                new StoredSet(StoredSet.CONVERSION_VERSION, setCode, setName, source, sourceDate,
                        tokenSetCode, printings.stream().map(ConvertedPrinting::printing).toList(),
                        List.copyOf(tokens)),
                new StoredSetDetails(StoredSet.CONVERSION_VERSION, setCode, fetchedAt,
                        printings.stream().map(ConvertedPrinting::details).toList()));
    }
}
