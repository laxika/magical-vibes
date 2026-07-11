package com.github.laxika.magicalvibes.scryfall;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.scryfall.ScryfallOracleLoader.TokenImageData;
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

/**
 * Loads the oracle registry from MTGJSON (https://mtgjson.com) set files instead of the Scryfall
 * API. Populates the exact same registries as {@link ScryfallOracleLoader} (oracle data, set
 * names, rarities, token images), so the two are interchangeable at startup: MTGJSON is either
 * selected via the {@code oracle.data-provider} property or used as a fallback when Scryfall is
 * unreachable.
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
public class MtgjsonOracleLoader {

    private static final Logger LOG = Logger.getLogger(MtgjsonOracleLoader.class.getName());
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    // MTGJSON keyword casing can differ from Scryfall's, so match case-insensitively
    private static final Map<String, Keyword> KEYWORD_MAP_LOWERCASE = new HashMap<>();

    static {
        ScryfallOracleLoader.KEYWORD_MAP.forEach((name, keyword) ->
                KEYWORD_MAP_LOWERCASE.put(name.toLowerCase(), keyword));
    }

    public static void loadAll(String cacheDir) {
        try {
            Path cachePath = Path.of(cacheDir);
            Files.createDirectories(cachePath);

            for (CardSet cardSet : CardSet.values()) {
                JsonNode setData = loadSet(cachePath, cardSet.getCode());

                if (setData.has("name")) {
                    CardSet.registerSetName(cardSet.getCode(), setData.get("name").asText());
                }

                // Key faces by collector number: transform DFCs are two entries sharing a number
                Map<String, JsonNode> frontFaces = new HashMap<>();
                Map<String, JsonNode> backFaces = new HashMap<>();
                if (setData.has("cards")) {
                    for (JsonNode cardNode : setData.get("cards")) {
                        String number = cardNode.get("number").asText();
                        if (cardNode.has("side") && "b".equals(cardNode.get("side").asText())) {
                            backFaces.put(number, cardNode);
                        } else {
                            frontFaces.put(number, cardNode);
                        }
                    }
                }

                for (Map.Entry<String, JsonNode> entry : frontFaces.entrySet()) {
                    JsonNode cardNode = entry.getValue();
                    if (cardNode.has("rarity")) {
                        ScryfallOracleLoader.registerRarity(cardSet.getCode(), entry.getKey(),
                                cardNode.get("rarity").asText());
                    }
                }

                for (CardPrinting printing : cardSet.getPrintings()) {
                    JsonNode front = frontFaces.get(printing.collectorNumber());
                    if (front == null) {
                        LOG.warning("No MTGJSON data for " + cardSet.getCode() + " #" + printing.collectorNumber());
                        continue;
                    }

                    Card tempCard = printing.factory().get();
                    String className = tempCard.getClass().getSimpleName();

                    Card.registerOracle(className, parseOracleData(front, false));

                    // If-absent for the same reason as the Scryfall loader: a back face may name
                    // a standalone card class whose own printing's data must win
                    String backFaceClassName = tempCard.getBackFaceClassName();
                    if (backFaceClassName != null) {
                        JsonNode back = backFaces.get(printing.collectorNumber());
                        if (back != null) {
                            Card.registerOracleIfAbsent(backFaceClassName, parseOracleData(back, true));
                        }
                    }
                }

                registerTokens(cardSet.getCode(), setData);
            }

            LOG.info("Oracle registry populated from MTGJSON for all card sets");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load MTGJSON oracle data", e);
        }
    }

    private static JsonNode loadSet(Path cachePath, String setCode) throws IOException, InterruptedException {
        Path cacheFile = cachePath.resolve("mtgjson-" + setCode.toLowerCase() + ".json");
        String json;

        if (Files.exists(cacheFile)) {
            LOG.info("Loading " + setCode + " from MTGJSON cache: " + cacheFile);
            json = Files.readString(cacheFile);
        } else {
            LOG.info("Fetching " + setCode + " from MTGJSON...");
            json = fetchFromMtgjson(setCode);
            Files.writeString(cacheFile, json);
            LOG.info("Cached " + setCode + " to: " + cacheFile);
        }

        JsonNode data = MAPPER.readTree(json).get("data");
        if (data == null) {
            throw new IOException("MTGJSON file for set " + setCode + " has no data node");
        }
        return data;
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

    /**
     * Parses one MTGJSON card entry (a single face) into OracleData. Mirrors
     * {@link ScryfallOracleLoader}'s front/back face handling: back faces prefer the color
     * indicator, drop the Transform keyword, and carry no loyalty or watermark.
     */
    static OracleData parseOracleData(JsonNode face, boolean isBackFace) {
        String name = face.has("faceName") ? face.get("faceName").asText() : face.get("name").asText();
        if (name.contains(" // ")) {
            name = name.substring(0, name.indexOf(" // "));
        }

        String manaCost = null;
        if (face.has("manaCost") && !face.get("manaCost").asText().isEmpty()) {
            manaCost = face.get("manaCost").asText();
        }

        String typeLine = face.has("type") ? face.get("type").asText() : "";
        ScryfallTypeLineParser.ParsedTypeLine parsed = ScryfallTypeLineParser.parse(typeLine);

        CardColor color;
        List<CardColor> colors;
        if (isBackFace && face.has("colorIndicator") && !face.get("colorIndicator").isEmpty()) {
            colors = parseColorArray(face.get("colorIndicator"));
        } else {
            colors = parseColorArray(face.get("colors"));
            // Lands have an empty colors array but derive color from colorIdentity (same rule
            // as the Scryfall loader, which does this with color_identity for lands only)
            if (colors.isEmpty() && typeLine.contains("Land") && face.has("colorIdentity")) {
                colors = parseColorArray(face.get("colorIdentity"));
            }
        }
        color = colors.isEmpty() ? null : colors.get(0);

        // MTGJSON brackets loyalty ability costs ("[+1]:", "[−X]:"); Scryfall does not
        String cardText = null;
        if (face.has("text")) {
            String normalized = face.get("text").asText()
                    .replaceAll("(?m)^\\[([+\\u2212-]?[0-9X]+)\\]:", "$1:");
            cardText = ScryfallOracleLoader.cleanCardText(normalized);
        }

        Integer power = ScryfallOracleLoader.parseIntField(face, "power");
        Integer toughness = ScryfallOracleLoader.parseIntField(face, "toughness");
        Integer loyalty = isBackFace ? null : ScryfallOracleLoader.parseIntField(face, "loyalty");

        Set<Keyword> keywords = parseKeywords(face);
        if (isBackFace) {
            keywords.remove(Keyword.TRANSFORM);
        }

        String watermark = null;
        if (!isBackFace && face.has("watermark") && !face.get("watermark").asText().isEmpty()) {
            // MTGJSON suffixes some watermarks with the set code ("set (DOM)"); Scryfall does not
            watermark = face.get("watermark").asText().replaceAll(" \\(.*\\)$", "");
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
                watermark
        );
    }

    /**
     * Registers creature-token image data from the set file's {@code tokens} array under the
     * Scryfall token set code ("t" + set code), which is what the frontend image fetch expects.
     */
    static void registerTokens(String setCode, JsonNode setData) {
        if (!setData.has("tokens") || setData.get("tokens").isEmpty()) {
            return;
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
            Integer power = ScryfallOracleLoader.parseIntField(tokenNode, "power");
            Integer toughness = ScryfallOracleLoader.parseIntField(tokenNode, "toughness");
            List<CardColor> colors = parseColorArray(tokenNode.get("colors"));
            CardColor color = colors.isEmpty() ? null : colors.get(0);

            String key = ScryfallOracleLoader.buildTokenKey(name, power, toughness, color);
            tokenMap.put(key, new TokenImageData(tokenSetCode, tokenNode.get("number").asText()));
        }

        if (!tokenMap.isEmpty()) {
            ScryfallOracleLoader.registerTokenImages(setCode, tokenMap);
            LOG.info("Loaded " + tokenMap.size() + " token images for set " + setCode + " from MTGJSON");
        }
    }

    private static List<CardColor> parseColorArray(JsonNode colorArray) {
        if (colorArray == null || !colorArray.isArray()) {
            return List.of();
        }
        // Scryfall serializes color arrays alphabetically (B,G,R,U,W); MTGJSON's ordering varies
        List<String> symbols = new ArrayList<>();
        for (JsonNode colorNode : colorArray) {
            symbols.add(colorNode.asText());
        }
        symbols.sort(null);
        List<CardColor> colors = new ArrayList<>();
        for (String symbol : symbols) {
            CardColor mapped = ScryfallOracleLoader.COLOR_MAP.get(symbol);
            if (mapped != null) {
                colors.add(mapped);
            }
        }
        return List.copyOf(colors);
    }

    private static Set<Keyword> parseKeywords(JsonNode face) {
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (face.has("keywords")) {
            for (JsonNode kw : face.get("keywords")) {
                Keyword keyword = KEYWORD_MAP_LOWERCASE.get(kw.asText().toLowerCase());
                if (keyword != null) {
                    keywords.add(keyword);
                } else {
                    LOG.fine("Unknown keyword from MTGJSON: " + kw.asText());
                }
            }
        }
        return keywords;
    }
}
