package com.github.laxika.magicalvibes.networking.message;
import com.github.laxika.magicalvibes.model.DeckFormat;
import java.util.List;
public record SaveDeckRequest(String name, List<DeckEntryInfo> entries, String id, DeckFormat format,
                              List<DeckEntryInfo> sideboard, DeckEntryInfo commander) {
    public SaveDeckRequest(String name, List<DeckEntryInfo> entries) { this(name, entries, null, DeckFormat.CASUAL, List.of(), null); }
    public SaveDeckRequest {
        format = format == null ? DeckFormat.CASUAL : format;
        entries = entries == null ? List.of() : List.copyOf(entries);
        sideboard = sideboard == null ? List.of() : List.copyOf(sideboard);
    }
    public record DeckEntryInfo(String setCode, String collectorNumber, int count) {}
}
