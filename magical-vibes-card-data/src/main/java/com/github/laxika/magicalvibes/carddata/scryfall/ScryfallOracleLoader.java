package com.github.laxika.magicalvibes.carddata.scryfall;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.OracleLoader;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.SetJsonCache;
import com.github.laxika.magicalvibes.carddata.SetOracleData;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.OracleData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Loads the oracle registry from the Scryfall API, caching each set's JSON under
 * {@code card-data.cache-dir} so later startups do not hit the network.
 *
 * <p>The default source: this bean exists unless {@code oracle.data-provider} explicitly selects
 * another one. The parse helpers stay {@code static} — they are pure functions over a
 * {@code JsonNode} and the unit tests call them directly, without a container.
 */
@Service
@ConditionalOnProperty(name = "oracle.data-provider", havingValue = "SCRYFALL", matchIfMissing = true)
public class ScryfallOracleLoader implements OracleLoader {

    private static final Logger LOG = Logger.getLogger(ScryfallOracleLoader.class.getName());
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final SetJsonCache cache;

    public ScryfallOracleLoader(@Value("${card-data.cache-dir:./card-data-cache}") String cacheDir) {
        this.cache = new SetJsonCache(cacheDir, "", "Scryfall", ScryfallOracleLoader::fetchFromScryfall);
    }

    @Override
    public SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers) {
        try {
            Map<String, JsonNode> cardsByCollectorNumber = parseSetJson(cache.get(setCode));

            String setName = null;
            if (!cardsByCollectorNumber.isEmpty()) {
                JsonNode firstCard = cardsByCollectorNumber.values().iterator().next();
                if (firstCard.has("set_name")) {
                    setName = firstCard.get("set_name").asText();
                }
            }

            // Rarity covers every card in the set, implemented or not.
            Map<String, String> rarities = new HashMap<>();
            for (Map.Entry<String, JsonNode> entry : cardsByCollectorNumber.entrySet()) {
                JsonNode cardNode = entry.getValue();
                if (cardNode.has("rarity")) {
                    rarities.put(entry.getKey(), cardNode.get("rarity").asText());
                }
            }

            // Oracle text is parsed only for printings the game implements.
            Map<String, OracleData> frontFaces = new HashMap<>();
            Map<String, OracleData> backFaces = new HashMap<>();
            for (String collectorNumber : implementedCollectorNumbers) {
                JsonNode cardNode = cardsByCollectorNumber.get(collectorNumber);
                if (cardNode == null) {
                    continue;
                }
                frontFaces.put(collectorNumber, parseOracleData(cardNode));

                OracleData backFaceData = parseBackFaceOracleData(cardNode);
                if (backFaceData != null) {
                    backFaces.put(collectorNumber, backFaceData);
                }
            }

            return new SetOracleData(setName, cardsByCollectorNumber.size(), rarities,
                    frontFaces, backFaces, loadTokens(setCode));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Scryfall oracle data for set " + setCode, e);
        }
    }

    /**
     * Token art for the set, from Scryfall's separate {@code t}-prefixed set. Never throws: a set
     * with no token set is normal, and a token fetch failing is not a reason to fail the card load.
     * Loads creature and non-creature tokens (Treasure, Food, …); skips emblems.
     */
    private Map<String, CardPrintingRegistry.TokenImageData> loadTokens(String setCode) {
        String tokenSetCode = "t" + setCode.toLowerCase();
        Map<String, CardPrintingRegistry.TokenImageData> tokenMap = new HashMap<>();
        try {
            for (Map.Entry<String, JsonNode> entry : parseSetJson(cache.get(tokenSetCode)).entrySet()) {
                JsonNode tokenNode = entry.getValue();
                String typeLine = tokenNode.has("type_line") ? tokenNode.get("type_line").asText() : "";
                if (typeLine.contains("Emblem")) continue;

                String name = tokenNode.get("name").asText();
                boolean isCreature = typeLine.contains("Creature");
                Integer power = isCreature ? CardDataSupport.parseIntField(tokenNode, "power") : null;
                Integer toughness = isCreature ? CardDataSupport.parseIntField(tokenNode, "toughness") : null;
                List<CardColor> colors = FaceOracleMapper.mapColors(
                        CardDataSupport.strings(tokenNode, "colors"));
                CardColor color = colors.isEmpty() ? null : colors.get(0);

                String key = CardPrintingRegistry.buildTokenKey(name, power, toughness, color);
                tokenMap.put(key, new CardPrintingRegistry.TokenImageData(tokenSetCode, entry.getKey()));
            }
            if (!tokenMap.isEmpty()) {
                LOG.info("Loaded " + tokenMap.size() + " token images for set " + setCode);
            }
        } catch (Exception e) {
            // Write empty cache to avoid hitting Scryfall again on next startup
            Path cacheFile = cache.fileFor(tokenSetCode);
            if (!Files.exists(cacheFile)) {
                try {
                    CardDataSupport.writeCacheFile(cacheFile, "[]");
                } catch (IOException ignored) {}
            }
            LOG.warning("Could not load token set " + tokenSetCode + ": " + e.getMessage());
        }
        return tokenMap;
    }

    private static String fetchFromScryfall(String setCode) throws IOException, InterruptedException {
        // Respect Scryfall rate limits: 50-100ms between requests
        Thread.sleep(100);

        List<JsonNode> allCards = new ArrayList<>();
        // include_variations=true pulls lettered printings that are variations of a base card
        // (Portal demo-game 6d/69d, etc.). Without it Scryfall omits them from set searches.
        String url = "https://api.scryfall.com/cards/search?q=set:" + setCode.toLowerCase()
                + "&unique=prints&include_variations=true";

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
        return FaceOracleMapper.toOracleData(toRawFrontFace(card), false);
    }

    /**
     * Flattens Scryfall's card object into the front face.
     *
     * <p>Scryfall ships one object per card, and which node a field lives on depends on the layout:
     * a transform DFC keeps face-specific fields under {@code card_faces[0]} while a normal card
     * has them top-level, hence the prefer-face-else-card ladder. {@code keywords} and
     * {@code color_identity} are the exceptions — they exist only at top level, and
     * {@code keywords} covers both faces combined.
     */
    private static RawFace toRawFrontFace(JsonNode card) {
        JsonNode face = getFrontFaceNode(card);
        return new RawFace(
                CardDataSupport.text(card, "name"),
                CardDataSupport.text(prefer(face, card, "mana_cost"), "mana_cost"),
                CardDataSupport.text(prefer(face, card, "type_line"), "type_line"),
                CardDataSupport.text(prefer(face, card, "oracle_text"), "oracle_text"),
                CardDataSupport.strings(prefer(face, card, "colors"), "colors"),
                List.of(),
                CardDataSupport.strings(card, "color_identity"),
                CardDataSupport.text(prefer(face, card, "power"), "power"),
                CardDataSupport.text(prefer(face, card, "toughness"), "toughness"),
                CardDataSupport.text(prefer(face, card, "loyalty"), "loyalty"),
                CardDataSupport.text(prefer(face, card, "defense"), "defense"),
                CardDataSupport.strings(card, "keywords"),
                CardDataSupport.text(card, "watermark"));
    }

    private static JsonNode prefer(JsonNode face, JsonNode card, String field) {
        return face.has(field) ? face : card;
    }

    /**
     * Parses oracle data for the back face of a double-faced card.
     * Returns null if the card is not a DFC or has no back face.
     */
    static OracleData parseBackFaceOracleData(JsonNode card) {
        RawFace back = toRawBackFace(card);
        return back == null ? null : FaceOracleMapper.toOracleData(back, true);
    }

    /**
     * Flattens {@code card_faces[1]} into a self-contained face, or null when the card has no back
     * face. Two fields are read from the parent rather than the face: {@code color_identity}, which
     * Scryfall puts on the top-level card only (reading it off the face silently skipped the land
     * colour fallback and left every transformed land colourless), and {@code keywords}, which is
     * the union of both faces and gets narrowed to this face's own by the mapper.
     */
    private static RawFace toRawBackFace(JsonNode card) {
        if (!card.has("card_faces") || !card.get("card_faces").isArray()
                || card.get("card_faces").size() < 2) {
            return null;
        }

        JsonNode face = card.get("card_faces").get(1);
        return new RawFace(
                CardDataSupport.text(face, "name"),
                CardDataSupport.text(face, "mana_cost"),
                CardDataSupport.text(face, "type_line"),
                CardDataSupport.text(face, "oracle_text"),
                CardDataSupport.strings(face, "colors"),
                CardDataSupport.strings(face, "color_indicator"),
                CardDataSupport.strings(card, "color_identity"),
                CardDataSupport.text(face, "power"),
                CardDataSupport.text(face, "toughness"),
                CardDataSupport.text(face, "loyalty"),
                CardDataSupport.text(face, "defense"),
                CardDataSupport.strings(card, "keywords"),
                CardDataSupport.text(card, "watermark"));
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

}

