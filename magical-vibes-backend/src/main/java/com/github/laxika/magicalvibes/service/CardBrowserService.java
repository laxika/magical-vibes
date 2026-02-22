package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.networking.message.BrowseCardInfo;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CardBrowserService {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private static final Map<String, String> COLOR_CODE_TO_NAME = Map.of(
            "W", "WHITE",
            "U", "BLUE",
            "B", "BLACK",
            "R", "RED",
            "G", "GREEN"
    );

    private static final Map<String, String> TYPE_MAP = Map.of(
            "Creature", "CREATURE",
            "Enchantment", "ENCHANTMENT",
            "Instant", "INSTANT",
            "Sorcery", "SORCERY",
            "Artifact", "ARTIFACT",
            "Land", "LAND",
            "Planeswalker", "PLANESWALKER"
    );

    private static final Map<String, String> SUPERTYPE_MAP = Map.of(
            "Basic", "BASIC",
            "Legendary", "LEGENDARY"
    );

    private final ConcurrentHashMap<String, List<BrowseCardInfo>> cache = new ConcurrentHashMap<>();

    @Value("${scryfall.cache-dir:./scryfall-cache}")
    private String cacheDir;

    public List<BrowseCardInfo> getCardsForSet(String setCode) {
        return cache.computeIfAbsent(setCode, this::loadCardsForSet);
    }

    private List<BrowseCardInfo> loadCardsForSet(String setCode) {
        try {
            Path cacheFile = Path.of(cacheDir, setCode.toLowerCase() + ".json");
            if (!Files.exists(cacheFile)) {
                return List.of();
            }

            String json = Files.readString(cacheFile);
            JsonNode array = MAPPER.readTree(json);

            if (!array.isArray()) {
                return List.of();
            }

            // Build set of implemented collector numbers
            CardSet cardSet = findCardSet(setCode);
            Set<String> implementedNumbers = Set.of();
            if (cardSet != null) {
                implementedNumbers = cardSet.getPrintings().stream()
                        .map(CardPrinting::collectorNumber)
                        .collect(Collectors.toSet());
            }

            List<BrowseCardInfo> cards = new ArrayList<>();
            for (JsonNode card : array) {
                String collectorNumber = card.get("collector_number").asText();

                // Skip special prints (star variants, promo suffixes, etc.)
                if (!collectorNumber.chars().allMatch(Character::isDigit)) {
                    continue;
                }

                // Skip showcase, extended art, borderless, promo, and full-art variants
                if (isSpecialPrint(card)) {
                    continue;
                }

                String name = card.get("name").asText();
                if (name.contains(" // ")) {
                    name = name.substring(0, name.indexOf(" // "));
                }

                String manaCost = card.has("mana_cost") ? card.get("mana_cost").asText() : null;
                if (manaCost != null && manaCost.isEmpty()) {
                    manaCost = null;
                }
                if (manaCost != null && manaCost.contains(" // ")) {
                    manaCost = manaCost.substring(0, manaCost.indexOf(" // "));
                }

                String typeLine = card.has("type_line") ? card.get("type_line").asText() : "";
                if (typeLine.contains(" // ")) {
                    typeLine = typeLine.substring(0, typeLine.indexOf(" // "));
                }

                String rarity = ScryfallOracleLoader.getRarity(setCode, collectorNumber);
                if (rarity == null) {
                    rarity = card.has("rarity") ? card.get("rarity").asText() : "common";
                }

                String power = card.has("power") ? card.get("power").asText() : null;
                String toughness = card.has("toughness") ? card.get("toughness").asText() : null;

                // Convert Scryfall color to our enum name (single-color only)
                String color = parseColor(card);

                // Oracle text (strip reminder text)
                String cardText = null;
                if (card.has("oracle_text") && !card.get("oracle_text").asText().isEmpty()) {
                    String rawText = card.get("oracle_text").asText()
                            .replaceAll(" *\\([^)]*\\)", "")
                            .replaceAll(" +\n", "\n")
                            .strip();
                    if (!rawText.isEmpty()) {
                        cardText = rawText;
                    }
                }

                // Keywords
                List<String> keywords = new ArrayList<>();
                if (card.has("keywords")) {
                    for (JsonNode kw : card.get("keywords")) {
                        keywords.add(kw.asText().toUpperCase().replace(" ", "_"));
                    }
                }

                // Parse type line into components
                String type = null;
                List<String> additionalTypes = new ArrayList<>();
                List<String> supertypes = new ArrayList<>();
                List<String> subtypes = new ArrayList<>();
                parseTypeLine(typeLine, type, additionalTypes, supertypes, subtypes);
                type = extractPrimaryType(typeLine);

                // Loyalty
                Integer loyalty = null;
                if (card.has("loyalty")) {
                    try {
                        loyalty = Integer.parseInt(card.get("loyalty").asText());
                    } catch (NumberFormatException e) {
                        loyalty = 0;
                    }
                }

                boolean implemented = implementedNumbers.contains(collectorNumber);

                cards.add(new BrowseCardInfo(
                        name, collectorNumber, setCode, manaCost, typeLine,
                        rarity, power, toughness, color, implemented,
                        cardText, keywords, type, additionalTypes, supertypes, subtypes, loyalty
                ));
            }

            return cards;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String parseColor(JsonNode card) {
        if (card.has("colors") && card.get("colors").isArray()) {
            JsonNode colors = card.get("colors");
            if (colors.size() == 1) {
                return COLOR_CODE_TO_NAME.get(colors.get(0).asText());
            }
            if (colors.isEmpty()) {
                // For lands, derive color from color_identity
                String typeLine = card.has("type_line") ? card.get("type_line").asText() : "";
                if (typeLine.contains("Land")
                        && card.has("color_identity") && card.get("color_identity").isArray()
                        && !card.get("color_identity").isEmpty()) {
                    String firstColor = card.get("color_identity").get(0).asText();
                    return COLOR_CODE_TO_NAME.get(firstColor);
                }
            }
        }
        return null;
    }

    private String extractPrimaryType(String typeLine) {
        // Split on em dash to get types part
        String typesPart = typeLine.contains(" \u2014 ")
                ? typeLine.substring(0, typeLine.indexOf(" \u2014 "))
                : typeLine;

        for (String word : typesPart.split("\\s+")) {
            String mapped = TYPE_MAP.get(word);
            if (mapped != null) {
                return mapped;
            }
        }
        return "CREATURE"; // fallback
    }

    private void parseTypeLine(String typeLine, String primaryType,
                               List<String> additionalTypes, List<String> supertypes, List<String> subtypes) {
        String typesPart;
        String subtypesPart = null;
        int dashIndex = typeLine.indexOf(" \u2014 ");
        if (dashIndex >= 0) {
            typesPart = typeLine.substring(0, dashIndex);
            subtypesPart = typeLine.substring(dashIndex + 3);
        } else {
            typesPart = typeLine;
        }

        boolean foundPrimary = false;
        for (String word : typesPart.split("\\s+")) {
            if (word.isEmpty()) continue;
            String supertype = SUPERTYPE_MAP.get(word);
            if (supertype != null) {
                supertypes.add(supertype);
                continue;
            }
            String type = TYPE_MAP.get(word);
            if (type != null) {
                if (!foundPrimary) {
                    foundPrimary = true;
                } else {
                    additionalTypes.add(type);
                }
            }
        }

        if (subtypesPart != null && !subtypesPart.isBlank()) {
            for (String word : subtypesPart.split("\\s+")) {
                if (!word.isEmpty()) {
                    subtypes.add(word.toUpperCase().replace("'", "").replace("-", "_"));
                }
            }
        }
    }

    private boolean isSpecialPrint(JsonNode card) {
        if (card.has("frame_effects") && card.get("frame_effects").isArray()) {
            for (JsonNode fe : card.get("frame_effects")) {
                String effect = fe.asText();
                if ("showcase".equals(effect) || "extendedart".equals(effect)) {
                    return true;
                }
            }
        }
        if (card.has("border_color") && "borderless".equals(card.get("border_color").asText())) {
            return true;
        }
        if (card.has("promo_types") && card.get("promo_types").isArray() && !card.get("promo_types").isEmpty()) {
            return true;
        }
        if (card.has("full_art") && card.get("full_art").asBoolean()) {
            return true;
        }
        return false;
    }

    private CardSet findCardSet(String setCode) {
        for (CardSet cs : CardSet.values()) {
            if (cs.getCode().equalsIgnoreCase(setCode)) {
                return cs;
            }
        }
        return null;
    }
}
