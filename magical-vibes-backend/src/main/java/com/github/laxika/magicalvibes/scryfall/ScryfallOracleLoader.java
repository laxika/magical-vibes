package com.github.laxika.magicalvibes.scryfall;

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

    // Rarity registry: "SET:collectorNumber" -> rarity (e.g. "common", "uncommon", "rare", "mythic")
    private static final Map<String, String> rarityRegistry = new HashMap<>();

    private static final Map<String, CardColor> COLOR_MAP = Map.of(
            "W", CardColor.WHITE,
            "U", CardColor.BLUE,
            "B", CardColor.BLACK,
            "R", CardColor.RED,
            "G", CardColor.GREEN
    );

    private static final Map<String, Keyword> KEYWORD_MAP = new HashMap<>();

    static {
        KEYWORD_MAP.put("Flying", Keyword.FLYING);
        KEYWORD_MAP.put("Reach", Keyword.REACH);
        KEYWORD_MAP.put("Defender", Keyword.DEFENDER);
        KEYWORD_MAP.put("Double strike", Keyword.DOUBLE_STRIKE);
        KEYWORD_MAP.put("First strike", Keyword.FIRST_STRIKE);
        KEYWORD_MAP.put("Flash", Keyword.FLASH);
        KEYWORD_MAP.put("Vigilance", Keyword.VIGILANCE);
        KEYWORD_MAP.put("Shroud", Keyword.SHROUD);
        KEYWORD_MAP.put("Changeling", Keyword.CHANGELING);
        KEYWORD_MAP.put("Fear", Keyword.FEAR);
        KEYWORD_MAP.put("Menace", Keyword.MENACE);
        KEYWORD_MAP.put("Indestructible", Keyword.INDESTRUCTIBLE);
        KEYWORD_MAP.put("Convoke", Keyword.CONVOKE);
        KEYWORD_MAP.put("Haste", Keyword.HASTE);
        KEYWORD_MAP.put("Lifelink", Keyword.LIFELINK);
        KEYWORD_MAP.put("Trample", Keyword.TRAMPLE);
        KEYWORD_MAP.put("Forestwalk", Keyword.FORESTWALK);
        KEYWORD_MAP.put("Mountainwalk", Keyword.MOUNTAINWALK);
        KEYWORD_MAP.put("Islandwalk", Keyword.ISLANDWALK);
        KEYWORD_MAP.put("Swampwalk", Keyword.SWAMPWALK);
    }

    public static void loadAll(String cacheDir) {
        try {
            Path cachePath = Path.of(cacheDir);
            Files.createDirectories(cachePath);

            for (CardSet cardSet : CardSet.values()) {
                Map<String, JsonNode> cardsByCollectorNumber = loadSet(cachePath, cardSet.getCode());

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
                        String key = cardSet.getCode() + ":" + entry.getKey();
                        rarityRegistry.put(key, cardNode.get("rarity").asText());
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
                }
            }

            LOG.info("Oracle registry populated with data for all card sets");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Scryfall oracle data", e);
        }
    }

    /**
     * Returns the rarity for a card in a set, e.g. "common", "uncommon", "rare", "mythic".
     */
    public static String getRarity(String setCode, String collectorNumber) {
        return rarityRegistry.get(setCode + ":" + collectorNumber);
    }

    private static Map<String, JsonNode> loadSet(Path cachePath, String setCode) throws IOException, InterruptedException {
        Path cacheFile = cachePath.resolve(setCode.toLowerCase() + ".json");
        String json;

        if (Files.exists(cacheFile)) {
            LOG.info("Loading " + setCode + " from cache: " + cacheFile);
            json = Files.readString(cacheFile);
        } else {
            LOG.info("Fetching " + setCode + " from Scryfall API...");
            json = fetchFromScryfall(setCode);
            Files.writeString(cacheFile, json);
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

    private static OracleData parseOracleData(JsonNode card) {
        // Name: handle double-faced cards
        String name = card.get("name").asText();
        if (name.contains(" // ")) {
            name = name.substring(0, name.indexOf(" // "));
        }

        // Mana cost
        String manaCost = null;
        if (card.has("mana_cost") && !card.get("mana_cost").asText().isEmpty()) {
            manaCost = card.get("mana_cost").asText();
            // Handle double-faced mana costs
            if (manaCost.contains(" // ")) {
                manaCost = manaCost.substring(0, manaCost.indexOf(" // "));
            }
        }

        // Type line
        String typeLine = card.get("type_line").asText();
        ScryfallTypeLineParser.ParsedTypeLine parsed = ScryfallTypeLineParser.parse(typeLine);

        // Color
        CardColor color = parseColor(card);

        // Oracle text (strip reminder text in parentheses)
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

        // Power/toughness (creatures only)
        Integer power = null;
        Integer toughness = null;
        if (card.has("power")) {
            try {
                power = Integer.parseInt(card.get("power").asText());
            } catch (NumberFormatException e) {
                // Handle * or other non-numeric power (e.g., Clone)
                power = 0;
            }
        }
        if (card.has("toughness")) {
            try {
                toughness = Integer.parseInt(card.get("toughness").asText());
            } catch (NumberFormatException e) {
                toughness = 0;
            }
        }

        // Loyalty (planeswalkers only)
        Integer loyalty = null;
        if (card.has("loyalty")) {
            try {
                loyalty = Integer.parseInt(card.get("loyalty").asText());
            } catch (NumberFormatException e) {
                loyalty = 0;
            }
        }

        // Keywords
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (card.has("keywords")) {
            for (JsonNode kw : card.get("keywords")) {
                Keyword keyword = KEYWORD_MAP.get(kw.asText());
                if (keyword != null) {
                    keywords.add(keyword);
                } else {
                    LOG.fine("Unknown keyword from Scryfall: " + kw.asText());
                }
            }
        }

        return new OracleData(
                name,
                parsed.type(),
                parsed.additionalTypes(),
                manaCost,
                color,
                parsed.supertypes(),
                parsed.subtypes(),
                cardText,
                power,
                toughness,
                keywords,
                loyalty
        );
    }

    private static CardColor parseColor(JsonNode card) {
        // Use colors array first
        if (card.has("colors") && card.get("colors").isArray() && !card.get("colors").isEmpty()) {
            String firstColor = card.get("colors").get(0).asText();
            return COLOR_MAP.get(firstColor);
        }

        // Lands have an empty colors array but can derive color from color_identity
        // (e.g. Forest has color_identity ["G"]). Other colorless cards (artifacts, Eldrazi)
        // should remain null even if they have a color_identity (e.g. Legacy Weapon has WUBRG identity).
        String typeLine = card.has("type_line") ? card.get("type_line").asText() : "";
        if (typeLine.contains("Land")
                && card.has("color_identity") && card.get("color_identity").isArray() && !card.get("color_identity").isEmpty()) {
            String firstColor = card.get("color_identity").get(0).asText();
            return COLOR_MAP.get(firstColor);
        }

        return null;
    }
}

