package com.github.laxika.magicalvibes.networking.message;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.DeckValidation;
public record DeckInfo(String id, String name, DeckFormat format, DeckValidation validation) {
    public DeckInfo(String id, String name) { this(id, name, DeckFormat.CASUAL, null); }
}
