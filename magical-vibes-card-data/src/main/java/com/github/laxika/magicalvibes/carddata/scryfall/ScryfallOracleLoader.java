package com.github.laxika.magicalvibes.carddata.scryfall;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.OracleTextNormalizer;
import com.github.laxika.magicalvibes.carddata.TypeLineParser;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ScryfallOracleLoader {

    private static final Logger LOG = Logger.getLogger(ScryfallOracleLoader.class.getName());
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    public static void loadAll(String cacheDir) {
        try {
            Path cachePath = Path.of(cacheDir);
            Files.createDirectories(cachePath);

            for (CardSet cardSet : CardSet.values()) {
                Map<String, JsonNode> cardsByCollectorNumber = loadSet(cachePath, cardSet.getCode());

                // Total cards in the set (all collector numbers) — the denominator for set-completeness.
                CardSet.registerSetCardTotal(cardSet.getCode(), cardsByCollectorNumber.size());

                // Register set name from the first card's set_name field
                if (!cardsByCollectorNumber.isEmpty()) {
                    JsonNode firstCard = cardsByCollectorNumber.values().iterator().next();
                    if (firstCard.has("set_name")) {
                        CardSet.registerSetName(cardSet.getCode(), firstCard.get("set_name").asText());
                    }
                }

                // Build rarity registry for all cards in the set
                for (Map.Entry<String, JsonNode> entry : cardsByCollectorNumber.entrySet()) {
                    JsonNode cardNode = entry.getValue();
                    if (cardNode.has("rarity")) {
                        CardPrintingRegistry.registerRarity(
                                cardSet.getCode(), entry.getKey(), cardNode.get("rarity").asText());
                    }
                }

                for (CardPrinting printing : cardSet.getPrintings()) {
                    JsonNode cardNode = cardsByCollectorNumber.get(printing.collectorNumber());
                    if (cardNode == null) {
                        LOG.warning("No Scryfall data for " + cardSet.getCode() + " #" + printing.collectorNumber());
                        continue;
                    }

                    // Create a temp card to get the class name
                    Card tempCard = printing.factory().get();
                    String className = tempCard.getClass().getSimpleName();

                    // Only register once per class name (same card in multiple printings)
                    OracleData oracleData = parseOracleData(cardNode);
                    Card.registerOracle(className, oracleData);

                    // Register back face oracle data for double-faced cards. If-absent: the back
                    // face may name a standalone card class (prepare spells reuse the real spell
                    // class), whose own printing registers richer data that must win regardless
                    // of set load order.
                    String backFaceClassName = tempCard.getBackFaceClassName();
                    if (backFaceClassName != null) {
                        OracleData backFaceData = parseBackFaceOracleData(cardNode);
                        if (backFaceData != null) {
                            Card.registerOracleIfAbsent(backFaceClassName, backFaceData);
                        }
                    }
                }
            }

            LOG.info("Oracle registry populated with data for all card sets");

            loadTokenSets(cachePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Scryfall oracle data", e);
        }
    }

    private static void loadTokenSets(Path cachePath) {
        for (CardSet cardSet : CardSet.values()) {
            String tokenSetCode = "t" + cardSet.getCode().toLowerCase();
            try {
                Map<String, JsonNode> tokens = loadSet(cachePath, tokenSetCode);
                Map<String, CardPrintingRegistry.TokenImageData> tokenMap = new HashMap<>();

                for (Map.Entry<String, JsonNode> entry : tokens.entrySet()) {
                    JsonNode tokenNode = entry.getValue();
                    String typeLine = tokenNode.has("type_line") ? tokenNode.get("type_line").asText() : "";
                    if (!typeLine.contains("Creature")) continue;

                    String name = tokenNode.get("name").asText();
                    Integer power = CardDataSupport.parseIntField(tokenNode, "power");
                    Integer toughness = CardDataSupport.parseIntField(tokenNode, "toughness");
                    CardColor color = parseColor(tokenNode);

                    String key = CardPrintingRegistry.buildTokenKey(name, power, toughness, color);
                    tokenMap.put(key, new CardPrintingRegistry.TokenImageData(tokenSetCode, entry.getKey()));
                }

                if (!tokenMap.isEmpty()) {
                    CardPrintingRegistry.registerTokenImages(cardSet.getCode(), tokenMap);
                    LOG.info("Loaded " + tokenMap.size() + " token images for set " + cardSet.getCode());
                }
            } catch (Exception e) {
                // Write empty cache to avoid hitting Scryfall again on next startup
                Path cacheFile = cachePath.resolve(tokenSetCode + ".json");
                if (!Files.exists(cacheFile)) {
                    try {
                        CardDataSupport.writeCacheFile(cacheFile, "[]");
                    } catch (IOException ignored) {}
                }
                LOG.warning("Could not load token set " + tokenSetCode + ": " + e.getMessage());
            }
        }
    }

    private static Map<String, JsonNode> loadSet(Path cachePath, String setCode) throws IOException, InterruptedException {
        Path cacheFile = cachePath.resolve(setCode.toLowerCase() + ".json");
        String json;

        if (Files.exists(cacheFile)) {
            LOG.info("Loading " + setCode + " from cache: " + cacheFile);
            json = Files.readString(cacheFile);
        } else {
            LOG.info("Fetching " + setCode + " from Scryfall API...");
            // Respect Scryfall rate limits: 50-100ms between requests
            Thread.sleep(100);
            json = fetchFromScryfall(setCode);
            CardDataSupport.writeCacheFile(cacheFile, json);
            LOG.info("Cached " + setCode + " to: " + cacheFile);
        }

        return parseSetJson(json);
    }

    private static String fetchFromScryfall(String setCode) throws IOException, InterruptedException {
        List<JsonNode> allCards = new ArrayList<>();
        String url = "https://api.scryfall.com/cards/search?q=set:" + setCode.toLowerCase() + "&unique=prints";

        try (HttpClient client = HttpClient.newHttpClient()) {
            while (url != null) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "MagicalVibes/1.0")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("Scryfall API returned " + response.statusCode() + " for set " + setCode);
                }

                JsonNode page = MAPPER.readTree(response.body());
                JsonNode data = page.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode card : data) {
                        allCards.add(card);
                    }
                }

                // Follow pagination
                if (page.has("has_more") && page.get("has_more").asBoolean()) {
                    url = page.get("next_page").asText();
                    // Respect Scryfall rate limits: 50-100ms between requests
                    Thread.sleep(100);
                } else {
                    url = null;
                }
            }
        }

        // Serialize all cards as a single JSON array for caching
        return MAPPER.writeValueAsString(allCards);
    }

    private static Map<String, JsonNode> parseSetJson(String json) {
        Map<String, JsonNode> result = new HashMap<>();
        JsonNode array = MAPPER.readTree(json);

        if (array.isArray()) {
            for (JsonNode card : array) {
                String collectorNumber = card.get("collector_number").asText();
                result.put(collectorNumber, card);
            }
        }

        return result;
    }

    static OracleData parseOracleData(JsonNode card) {
        // For transform DFCs, face-specific fields (power, toughness, oracle_text) are in card_faces, not top-level
        JsonNode faceNode = getFrontFaceNode(card);

        // Name: handle double-faced cards
        String name = card.get("name").asText();
        if (name.contains(" // ")) {
            name = name.substring(0, name.indexOf(" // "));
        }

        // Mana cost — prefer face node for DFCs
        String manaCost = null;
        JsonNode manaCostSource = faceNode.has("mana_cost") ? faceNode : card;
        if (manaCostSource.has("mana_cost") && !manaCostSource.get("mana_cost").asText().isEmpty()) {
            manaCost = manaCostSource.get("mana_cost").asText();
            // Handle double-faced mana costs
            if (manaCost.contains(" // ")) {
                manaCost = manaCost.substring(0, manaCost.indexOf(" // "));
            }
        }

        // Type line — prefer face node for split/transform DFCs
        JsonNode typeLineSource = faceNode.has("type_line") ? faceNode : card;
        String typeLine = typeLineSource.get("type_line").asText();
        TypeLineParser.ParsedTypeLine parsed = TypeLineParser.parse(typeLine);

        // Color — prefer face node for DFCs
        JsonNode colorSource = faceNode.has("colors") ? faceNode : card;
        CardColor color = parseColor(colorSource);
        List<CardColor> colors = parseColors(colorSource);

        // Oracle text (strip reminder text in parentheses) — prefer face node for DFCs
        // Keywords live at top level (combined for both faces), so they are the keyword source
        // even when the text comes from a face node.
        String cardText = parseCardText(faceNode.has("oracle_text") ? faceNode : card, card);

        // Power/toughness (creatures only) — prefer face node for DFCs
        JsonNode ptSource = faceNode.has("power") ? faceNode : card;
        Integer power = CardDataSupport.parseIntField(ptSource, "power");
        Integer toughness = CardDataSupport.parseIntField(ptSource, "toughness");

        // Loyalty (planeswalkers) / defense (battles) — prefer face node for DFCs
        JsonNode loyaltySource = faceNode.has("loyalty") ? faceNode : card;
        Integer loyalty = CardDataSupport.parseIntField(loyaltySource, "loyalty");
        JsonNode defenseSource = faceNode.has("defense") ? faceNode : card;
        Integer defense = CardDataSupport.parseIntField(defenseSource, "defense");

        // Keywords — from top level (combined for both faces)
        Set<Keyword> keywords = parseKeywords(card);

        // Watermark
        String watermark = null;
        if (card.has("watermark") && !card.get("watermark").asText().isEmpty()) {
            watermark = card.get("watermark").asText();
        }

        return new OracleData(
                name,
                parsed.type(),
                parsed.additionalTypes(),
                manaCost,
                color,
                colors,
                parsed.supertypes(),
                parsed.subtypes(),
                cardText,
                power,
                toughness,
                keywords,
                loyalty,
                defense,
                watermark
        );
    }

    /**
     * Parses oracle data for the back face of a double-faced card.
     * Returns null if the card is not a DFC or has no back face.
     */
    static OracleData parseBackFaceOracleData(JsonNode card) {
        if (!card.has("card_faces") || !card.get("card_faces").isArray()
                || card.get("card_faces").size() < 2) {
            return null;
        }

        JsonNode face = card.get("card_faces").get(1);

        String name = face.get("name").asText();

        String manaCost = null;
        if (face.has("mana_cost") && !face.get("mana_cost").asText().isEmpty()) {
            manaCost = face.get("mana_cost").asText();
        }

        // Parse type line from back face
        String typeLine = face.has("type_line") ? face.get("type_line").asText() : "";
        TypeLineParser.ParsedTypeLine parsed = TypeLineParser.parse(typeLine);

        // Color — use color_indicator if present, otherwise colors
        CardColor color = null;
        List<CardColor> colors = List.of();
        if (face.has("color_indicator") && face.get("color_indicator").isArray()
                && !face.get("color_indicator").isEmpty()) {
            String firstColor = face.get("color_indicator").get(0).asText();
            color = CardDataSupport.COLOR_MAP.get(firstColor);
            List<CardColor> indicatorColors = new ArrayList<>();
            for (JsonNode colorNode : face.get("color_indicator")) {
                CardColor mapped = CardDataSupport.COLOR_MAP.get(colorNode.asText());
                if (mapped != null) indicatorColors.add(mapped);
            }
            colors = List.copyOf(indicatorColors);
        } else if (face.has("colors")) {
            color = parseColor(face);
            colors = parseColors(face);
        }

        String cardText = parseCardText(face, card);

        Integer power = CardDataSupport.parseIntField(face, "power");
        Integer toughness = CardDataSupport.parseIntField(face, "toughness");

        // Back faces use front face keywords minus Transform, plus their own abilities
        // Scryfall keywords at top level cover both faces; we re-parse from top level
        Set<Keyword> keywords = parseKeywords(card);
        keywords.remove(Keyword.TRANSFORM);
        // Prepared belongs to the front face that owns the prepare spell, never to the spell itself
        keywords.remove(Keyword.PREPARED);

        return new OracleData(
                name,
                parsed.type(),
                parsed.additionalTypes(),
                manaCost,
                color,
                colors,
                parsed.supertypes(),
                parsed.subtypes(),
                cardText,
                power,
                toughness,
                keywords,
                null,
                null,
                null
        );
    }

    /**
     * Returns card_faces[0] for transform DFCs and split cards (including aftermath),
     * or the card itself for normal cards.
     */
    private static JsonNode getFrontFaceNode(JsonNode card) {
        if (card.has("card_faces") && card.has("layout")) {
            String layout = card.get("layout").asText();
            if ("transform".equals(layout) || "split".equals(layout) || "flip".equals(layout)
                    || "adventure".equals(layout) || "modal_dfc".equals(layout)
                    || "prepare".equals(layout)) {
                return card.get("card_faces").get(0);
            }
        }
        return card;
    }

    private static String parseCardText(JsonNode textSource, JsonNode keywordSource) {
        if (textSource.has("oracle_text") && !textSource.get("oracle_text").asText().isEmpty()) {
            String cleaned = OracleTextNormalizer.cleanCardText(textSource.get("oracle_text").asText());
            return OracleTextNormalizer.capitalizeKeywordLines(cleaned, keywordSource);
        }
        return null;
    }


    private static Set<Keyword> parseKeywords(JsonNode card) {
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (card.has("keywords")) {
            for (JsonNode kw : card.get("keywords")) {
                Keyword keyword = CardDataSupport.KEYWORD_MAP.get(kw.asText());
                if (keyword != null) {
                    keywords.add(keyword);
                } else {
                    LOG.fine("Unknown keyword from Scryfall: " + kw.asText());
                }
            }
        }
        return keywords;
    }

    private static CardColor parseColor(JsonNode card) {
        // Use colors array first
        if (card.has("colors") && card.get("colors").isArray() && !card.get("colors").isEmpty()) {
            String firstColor = card.get("colors").get(0).asText();
            return CardDataSupport.COLOR_MAP.get(firstColor);
        }

        // Lands have an empty colors array but can derive color from color_identity
        // (e.g. Forest has color_identity ["G"]). Other colorless cards (artifacts, Eldrazi)
        // should remain null even if they have a color_identity (e.g. Legacy Weapon has WUBRG identity).
        String typeLine = card.has("type_line") ? card.get("type_line").asText() : "";
        if (typeLine.contains("Land")
                && card.has("color_identity") && card.get("color_identity").isArray() && !card.get("color_identity").isEmpty()) {
            String firstColor = card.get("color_identity").get(0).asText();
            return CardDataSupport.COLOR_MAP.get(firstColor);
        }

        return null;
    }

    private static List<CardColor> parseColors(JsonNode card) {
        List<CardColor> colors = new ArrayList<>();
        if (card.has("colors") && card.get("colors").isArray()) {
            for (JsonNode colorNode : card.get("colors")) {
                CardColor mapped = CardDataSupport.COLOR_MAP.get(colorNode.asText());
                if (mapped != null) {
                    colors.add(mapped);
                }
            }
        }
        if (colors.isEmpty()) {
            // For lands, derive from color_identity (same logic as parseColor)
            String typeLine = card.has("type_line") ? card.get("type_line").asText() : "";
            if (typeLine.contains("Land")
                    && card.has("color_identity") && card.get("color_identity").isArray()) {
                for (JsonNode colorNode : card.get("color_identity")) {
                    CardColor mapped = CardDataSupport.COLOR_MAP.get(colorNode.asText());
                    if (mapped != null) {
                        colors.add(mapped);
                    }
                }
            }
        }
        return List.copyOf(colors);
    }
}

