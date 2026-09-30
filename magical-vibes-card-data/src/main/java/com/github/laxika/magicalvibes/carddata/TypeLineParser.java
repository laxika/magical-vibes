package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class TypeLineParser {

    private static final Logger LOG = Logger.getLogger(TypeLineParser.class.getName());

    private static final Map<String, CardSupertype> SUPERTYPE_MAP = Map.of(
            "Basic", CardSupertype.BASIC,
            "Legendary", CardSupertype.LEGENDARY,
            "Snow", CardSupertype.SNOW,
            "Ongoing", CardSupertype.ONGOING,
            "World", CardSupertype.WORLD
    );

    private static final Map<String, CardType> TYPE_MAP = java.util.Arrays.stream(CardType.values())
            .collect(java.util.stream.Collectors.toUnmodifiableMap(CardType::getDisplayName, type -> type));

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

        // Some playtest cards retain the legacy creature type line.
        if (typeLine.startsWith("Summon ")) {
            typeLine = "Creature \u2014 " + typeLine.substring("Summon ".length());
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
                if (cardType == CardType.KINDRED) {
                    additionalTypes.add(cardType);
                } else if (type == null) {
                    type = cardType;
                } else {
                    additionalTypes.add(cardType);
                }
            }
        }

        // Parse subtypes
        if (subtypesPart != null && !subtypesPart.isBlank()) {
            if (type != null && type.isPlanar()) {
                CardSubtype subtype = SUBTYPE_MAP.get(subtypesPart);
                if (subtype != null) {
                    subtypes.add(subtype);
                }
            } else {
                String[] words = subtypesPart.split("\\s+");
                for (int index = 0; index < words.length;) {
                    CardSubtype match = null;
                    int nextIndex = index + 1;
                    for (int end = words.length; end > index; end--) {
                        String candidate = String.join(" ", java.util.Arrays.copyOfRange(words, index, end));
                        match = SUBTYPE_MAP.get(candidate);
                        if (match != null) {
                            nextIndex = end;
                            break;
                        }
                    }
                    if (match != null) {
                        subtypes.add(match);
                    } else {
                        LOG.fine("Unknown subtype from Scryfall: " + words[index]);
                    }
                    index = nextIndex;
                }
            }
        }

        return new ParsedTypeLine(supertypes, type, additionalTypes, subtypes);
    }
}

