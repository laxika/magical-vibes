package com.github.laxika.magicalvibes.scryfall;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ScryfallTypeLineParser {

    private static final Logger LOG = Logger.getLogger(ScryfallTypeLineParser.class.getName());

    private static final Map<String, CardSupertype> SUPERTYPE_MAP = Map.of(
            "Basic", CardSupertype.BASIC,
            "Legendary", CardSupertype.LEGENDARY
    );

    private static final Map<String, CardType> TYPE_MAP = Map.of(
            "Creature", CardType.CREATURE,
            "Enchantment", CardType.ENCHANTMENT,
            "Instant", CardType.INSTANT,
            "Sorcery", CardType.SORCERY,
            "Artifact", CardType.ARTIFACT,
            "Land", CardType.LAND,
            "Planeswalker", CardType.PLANESWALKER
    );

    private static final Map<String, CardSubtype> SUBTYPE_MAP;

    static {
        SUBTYPE_MAP = new java.util.HashMap<>();
        for (CardSubtype subtype : CardSubtype.values()) {
            SUBTYPE_MAP.put(subtype.getDisplayName(), subtype);
        }
    }

    public record ParsedTypeLine(
            Set<CardSupertype> supertypes,
            CardType type,
            Set<CardType> additionalTypes,
            List<CardSubtype> subtypes
    ) {}

    public static ParsedTypeLine parse(String typeLine) {
        // Handle double-faced cards: take front face only
        if (typeLine.contains(" // ")) {
            typeLine = typeLine.substring(0, typeLine.indexOf(" // "));
        }

        Set<CardSupertype> supertypes = EnumSet.noneOf(CardSupertype.class);
        CardType type = null;
        Set<CardType> additionalTypes = EnumSet.noneOf(CardType.class);
        List<CardSubtype> subtypes = new ArrayList<>();

        // Split into type part and subtype part on " — " (em dash with spaces)
        String typesPart;
        String subtypesPart = null;
        int dashIndex = typeLine.indexOf(" \u2014 ");
        if (dashIndex >= 0) {
            typesPart = typeLine.substring(0, dashIndex);
            subtypesPart = typeLine.substring(dashIndex + 3);
        } else {
            typesPart = typeLine;
        }

        // Parse types: "Legendary Creature" → supertype=LEGENDARY, type=CREATURE
        // "Basic Land" → supertype=BASIC, type=LAND (parsed normally via maps)
        // "Artifact Creature" → type=ARTIFACT, additionalTypes={CREATURE}
        String[] typeWords = typesPart.split("\\s+");
        for (String word : typeWords) {
            if (word.isEmpty()) continue;

            CardSupertype supertype = SUPERTYPE_MAP.get(word);
            if (supertype != null) {
                supertypes.add(supertype);
                continue;
            }

            CardType cardType = TYPE_MAP.get(word);
            if (cardType != null) {
                if (type == null) {
                    type = cardType;
                } else {
                    additionalTypes.add(cardType);
                }
            }
        }

        // Parse subtypes
        if (subtypesPart != null && !subtypesPart.isBlank()) {
            String[] subtypeWords = subtypesPart.split("\\s+");
            for (String word : subtypeWords) {
                if (word.isEmpty()) continue;
                CardSubtype subtype = SUBTYPE_MAP.get(word);
                if (subtype != null) {
                    subtypes.add(subtype);
                } else {
                    LOG.fine("Unknown subtype from Scryfall: " + word);
                }
            }
        }

        return new ParsedTypeLine(supertypes, type, additionalTypes, subtypes);
    }
}
