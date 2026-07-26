package com.github.laxika.magicalvibes.carddata.mtgjson;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry.TokenImageData;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.OracleLoader;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.SetJsonCache;
import com.github.laxika.magicalvibes.carddata.SetOracleData;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Loads the oracle registry from MTGJSON (https://mtgjson.com) set files instead of the Scryfall
 * API. Populates the exact same registries as the Scryfall loader (oracle data via
 * {@code Card.registerOracle}, set names, {@link CardPrintingRegistry}), so the two are
 * interchangeable at startup: MTGJSON is selected by setting {@code oracle.data-provider=MTGJSON},
 * which CI does, since it needs oracle data without depending on the Scryfall API being up.
 *
 * <p>Structural differences from Scryfall handled here:
 * <ul>
 * <li>A set is a single {@code https://mtgjson.com/api/v5/{SET}.json} file (no pagination, no
 * rate limiting); the payload of interest sits under the top-level {@code data} node.</li>
 * <li>Double-faced cards are two card entries sharing a collector number, tagged {@code side}
 * "a"/"b", instead of one entry with a {@code card_faces} array. Each face entry's
 * {@code keywords} already covers both faces combined, matching Scryfall's top-level list.</li>
 * <li>Tokens live in the set file's {@code tokens} array instead of a separate "t"-prefixed set.
 * Their collector numbers match Scryfall's token sets, so the registered {@link TokenImageData}
 * still resolves to valid Scryfall image URLs.</li>
 * </ul>
 */
@Service
@ConditionalOnProperty(name = "oracle.data-provider", havingValue = "MTGJSON")
public class MtgjsonOracleLoader implements OracleLoader {

    private static final Logger LOG = Logger.getLogger(MtgjsonOracleLoader.class.getName());
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final SetJsonCache cache;

    public MtgjsonOracleLoader(@Value("${card-data.cache-dir:./card-data-cache}") String cacheDir) {
        this.cache = new SetJsonCache(cacheDir, "mtgjson-", "MTGJSON", MtgjsonOracleLoader::fetchFromMtgjson);
    }

    @Override
    public SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers) {
        try {
            JsonNode setData = MAPPER.readTree(cache.get(setCode)).get("data");
            if (setData == null) {
                throw new IOException("MTGJSON file for set " + setCode + " has no data node");
            }

            String setName = setData.has("name") ? setData.get("name").asText() : null;

            FaceIndex faces = indexFacesByCollectorNumber(setData.get("cards"));
            Map<String, JsonNode> frontFaceNodes = faces.frontFaces();
            Map<String, JsonNode> backFaceNodes = faces.backFaces();

            // Rarity covers every card in the set, implemented or not.
            Map<String, String> rarities = new HashMap<>();
            for (Map.Entry<String, JsonNode> entry : frontFaceNodes.entrySet()) {
                JsonNode cardNode = entry.getValue();
                if (cardNode.has("rarity")) {
                    rarities.put(entry.getKey(), cardNode.get("rarity").asText());
                }
            }

            // Oracle text is parsed only for printings the game implements.
            Map<String, OracleData> frontFaces = new HashMap<>();
            Map<String, OracleData> backFaces = new HashMap<>();
            for (String collectorNumber : implementedCollectorNumbers) {
                JsonNode front = frontFaceNodes.get(collectorNumber);
                if (front == null) {
                    continue;
                }
                frontFaces.put(collectorNumber, parseOracleData(front, false));

                JsonNode back = backFaceNodes.get(collectorNumber);
                if (back != null) {
                    backFaces.put(collectorNumber, parseOracleData(back, true));
                }
            }

            // Total cards in the set (one entry per collector number, meld results included —
            // the same count Scryfall yields) — the set-completeness denominator.
            return new SetOracleData(setName, frontFaceNodes.size(), rarities,
                    frontFaces, backFaces, parseTokens(setCode, setData));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load MTGJSON oracle data for set " + setCode, e);
        }
    }

    /** The set's card entries keyed by collector number, one map per side. */
    record FaceIndex(Map<String, JsonNode> frontFaces, Map<String, JsonNode> backFaces) {
    }

    /**
     * Keys a set file's card entries by collector number. Transform DFCs are two entries sharing
     * a number (side "a"/"b"), so the sides land in separate maps. Meld results (INR's Brisela,
     * Voice of Nightmares) are also tagged side "b" but own their collector number ("14b") with
     * no side-"a" partner at it — those are standalone printings, so they are promoted into the
     * front-face map, matching Scryfall, which serves them as ordinary card objects under the
     * same numbers (printing lookup, rarity registration and the set total all see them).
     */
    static FaceIndex indexFacesByCollectorNumber(JsonNode cards) {
        Map<String, JsonNode> frontFaces = new HashMap<>();
        Map<String, JsonNode> backFaces = new HashMap<>();
        if (cards != null) {
            for (JsonNode cardNode : cards) {
                String number = cardNode.get("number").asText();
                if (cardNode.has("side") && "b".equals(cardNode.get("side").asText())) {
                    backFaces.put(number, cardNode);
                } else {
                    frontFaces.put(number, cardNode);
                }
            }
            backFaces.forEach(frontFaces::putIfAbsent);
        }
        return new FaceIndex(frontFaces, backFaces);
    }

    private static String fetchFromMtgjson(String setCode) throws IOException, InterruptedException {
        String url = "https://mtgjson.com/api/v5/" + setCode.toUpperCase() + ".json";

        try (HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "MagicalVibes/1.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("MTGJSON returned " + response.statusCode() + " for set " + setCode);
            }

            return response.body();
        }
    }

    static OracleData parseOracleData(JsonNode face, boolean isBackFace) {
        return FaceOracleMapper.toOracleData(toRawFace(face), isBackFace);
    }

    /**
     * Flattens one MTGJSON card entry into a face. Close to a field rename — MTGJSON already splits
     * faces upstream, so each entry is self-contained — plus this loader's two compensating quirks:
     * the loyalty-bracket syntax and the watermark suffix.
     */
    private static RawFace toRawFace(JsonNode face) {
        String text = CardDataSupport.text(face, "text");
        if (text != null) {
            // MTGJSON brackets loyalty ability costs ("[+1]:", "[−X]:"); Scryfall does not
            text = text.replaceAll("(?m)^\\[([+\\u2212-]?[0-9X]+)\\]:", "$1:");
        }

        String watermark = CardDataSupport.text(face, "watermark");
        if (watermark != null) {
            // MTGJSON suffixes some watermarks with the set code ("set (DOM)"); Scryfall does not
            watermark = watermark.replaceAll(" \\(.*\\)$", "");
        }

        return new RawFace(
                face.has("faceName") ? face.get("faceName").asText() : CardDataSupport.text(face, "name"),
                CardDataSupport.text(face, "manaCost"),
                CardDataSupport.text(face, "type"),
                text,
                sortedSymbols(face, "colors"),
                sortedSymbols(face, "colorIndicator"),
                sortedSymbols(face, "colorIdentity"),
                CardDataSupport.text(face, "power"),
                CardDataSupport.text(face, "toughness"),
                CardDataSupport.text(face, "loyalty"),
                CardDataSupport.text(face, "defense"),
                CardDataSupport.strings(face, "keywords"),
                watermark);
    }

    /**
     * Scryfall serializes colour arrays alphabetically (B,G,R,U,W) and MTGJSON's ordering varies,
     * so sort here. Ordering is a provider quirk, and Scryfall is the default provider, so its
     * ordering is the reference the other has to match.
     */
    private static List<String> sortedSymbols(JsonNode face, String field) {
        List<String> symbols = new ArrayList<>(CardDataSupport.strings(face, field));
        symbols.sort(null);
        return symbols;
    }

    /**
     * Creature-token image data from the set file's {@code tokens} array, keyed under the Scryfall
     * token set code ("t" + set code), which is what the frontend image fetch expects. Unlike
     * Scryfall, MTGJSON ships tokens inline in the set file, so this needs no second fetch.
     */
    static Map<String, TokenImageData> parseTokens(String setCode, JsonNode setData) {
        if (!setData.has("tokens") || setData.get("tokens").isEmpty()) {
            return Map.of();
        }

        String tokenSetCode = "t" + setCode.toLowerCase();
        Map<String, TokenImageData> tokenMap = new HashMap<>();

        for (JsonNode tokenNode : setData.get("tokens")) {
            if (tokenNode.has("side") && !"a".equals(tokenNode.get("side").asText())) {
                continue;
            }
            String typeLine = tokenNode.has("type") ? tokenNode.get("type").asText() : "";
            if (!typeLine.contains("Creature")) {
                continue;
            }

            String name = tokenNode.has("faceName")
                    ? tokenNode.get("faceName").asText()
                    : tokenNode.get("name").asText();
            Integer power = CardDataSupport.parseIntField(tokenNode, "power");
            Integer toughness = CardDataSupport.parseIntField(tokenNode, "toughness");
            List<CardColor> colors = FaceOracleMapper.mapColors(sortedSymbols(tokenNode, "colors"));
            CardColor color = colors.isEmpty() ? null : colors.get(0);

            String key = CardPrintingRegistry.buildTokenKey(name, power, toughness, color);
            tokenMap.put(key, new TokenImageData(tokenSetCode, tokenNode.get("number").asText()));
        }

        if (!tokenMap.isEmpty()) {
            LOG.info("Loaded " + tokenMap.size() + " token images for set " + setCode + " from MTGJSON");
        }
        return tokenMap;
    }

}
