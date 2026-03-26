package com.github.laxika.magicalvibes.networking.message;

import java.util.List;

public record SaveDeckRequest(String name, List<DeckEntryInfo> entries) {

    public record DeckEntryInfo(String setCode, String collectorNumber, int count) {
    }
}
