package com.github.laxika.magicalvibes.carddata.mtgjson;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.SetSource;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.carddata.store.StoredToken;
import org.springframework.stereotype.Component;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Downloads a set from MTGJSON (https://mtgjson.com) set files.
 *
 * <p>Structural differences from Scryfall handled here:
 * <ul>
 * <li>A set is a single {@code https://mtgjson.com/api/v5/{SET}.json} file (no pagination, no
 * rate limiting); the payload of interest sits under the top-level {@code data} node.</li>
 * <li>Double-faced cards are two card entries sharing a collector number, tagged {@code side}
 * "a"/"b", instead of one entry with a {@code card_faces} array. Each face entry's
 * {@code keywords} already covers both faces combined, matching Scryfall's top-level list.</li>
 * <li>Tokens live in the set file's {@code tokens} array instead of a separate "t"-prefixed set.
 * Their collector numbers match Scryfall's token sets, so token images still resolve to valid
 * Scryfall image URLs.</li>
 * <li>Planeswalker-deck printings carry no promo type; see {@link #promoTypes}.</li>
 * </ul>
 */
@Component
public class MtgjsonSetSource implements SetSource {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(2);
    /** A generic browser User-Agent; MTGJSON's edge rejects the old app-specific one with 403. */
    static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36";

    @Override
    public OracleDataProvider provider() {
        return OracleDataProvider.MTGJSON;
    }

    @Override
    public StoredSet download(String setCode, Path tempDir) throws IOException, InterruptedException {
        String sourceSetCode = "MB1".equalsIgnoreCase(setCode) ? "CMB1" : setCode;
        Path file = tempDir.resolve("mtgjson-" + sourceSetCode.toLowerCase(Locale.ROOT) + ".json");
        try (HttpClient client = createHttpClient()) {
            HttpResponse<Path> response = client.send(
                    createRequest(sourceSetCode), HttpResponse.BodyHandlers.ofFile(file));
            if (response.statusCode() != 200) {
                throw new IOException("MTGJSON returned " + response.statusCode() + " for set " + sourceSetCode);
            }
        }
        return convert(setCode, sourceSetCode, MAPPER.readTree(Files.readString(file)), Instant.now());
    }

    static StoredSet convert(String setCode, String sourceSetCode, JsonNode root, Instant fetchedAt)
            throws IOException {
        JsonNode setData = root.get("data");
        if (setData == null) {
            throw new IOException("MTGJSON file for set " + sourceSetCode + " has no data node");
        }
        JsonNode meta = root.get("meta");

        FaceIndex faces = indexFacesByCollectorNumber(setData.get("cards"));
        List<StoredPrinting> printings = new ArrayList<>();
        faces.frontFaces().forEach((number, front) -> {
            // A promoted meld result is its own front face, not its own back face.
            JsonNode back = faces.backFaces().get(number) == front ? null : faces.backFaces().get(number);
            List<JsonNode> halves = faces.splitHalves().get(number);
            printings.add(new StoredPrinting(
                    number,
                    CardDataSupport.text(front, "rarity"),
                    List.copyOf(faces.faceNamesByCollectorNumber().getOrDefault(number, List.of())),
                    toRawFace(front),
                    back == null ? null : toRawBackFace(back),
                    halves == null ? List.of() : halves.stream().map(MtgjsonSetSource::toRawSplitHalf).toList(),
                    new PrintVariant(
                            promoTypes(front),
                            CardDataSupport.strings(front, "frameEffects"),
                            CardDataSupport.text(front, "borderColor"),
                            front.has("isFullArt") && front.get("isFullArt").asBoolean()),
                    parseLegalities(front)));
        });

        List<StoredToken> tokens = new ArrayList<>();
        JsonNode tokenNodes = setData.get("tokens");
        if (tokenNodes != null && tokenNodes.isArray()) {
            for (JsonNode token : tokenNodes) {
                if (token.has("number")) {
                    tokens.add(new StoredToken(token.get("number").asText(),
                            CardDataSupport.text(token, "side"), toRawFace(token)));
                }
            }
        }

        return new StoredSet(StoredSet.FORMAT_VERSION, setCode, CardDataSupport.text(setData, "name"),
                OracleDataProvider.MTGJSON, meta == null ? null : CardDataSupport.text(meta, "date"),
                fetchedAt, "t" + sourceSetCode.toLowerCase(Locale.ROOT), printings, tokens);
    }

    /**
     * The set's card entries keyed by collector number, one map per side, in file order.
     *
     * @param splitHalves every half of a split card, in side order, keyed by collector number
     */
    record FaceIndex(Map<String, JsonNode> frontFaces, Map<String, JsonNode> backFaces,
                     Map<String, List<String>> faceNamesByCollectorNumber,
                     Map<String, List<JsonNode>> splitHalves) {
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
        Map<String, JsonNode> frontFaces = new LinkedHashMap<>();
        Map<String, JsonNode> backFaces = new LinkedHashMap<>();
        Map<String, List<String>> faceNames = new HashMap<>();
        Map<String, List<JsonNode>> splitHalves = new HashMap<>();
        if (cards != null) {
            for (JsonNode cardNode : cards) {
                String number = cardNode.get("number").asText();
                String side = cardNode.has("side") ? cardNode.get("side").asText() : null;
                String faceName = cardNode.has("faceName")
                        ? cardNode.get("faceName").asText()
                        : cardNode.has("name") ? cardNode.get("name").asText() : null;
                if (faceName != null) {
                    faceNames.computeIfAbsent(number, ignored -> new ArrayList<>()).add(faceName);
                }
                if (FaceOracleMapper.isSplitLayout(CardDataSupport.text(cardNode, "layout"))) {
                    splitHalves.computeIfAbsent(number, ignored -> new ArrayList<>()).add(cardNode);
                }
                if ("b".equals(side)) {
                    backFaces.put(number, cardNode);
                } else if (!"c".equals(side)) {
                    frontFaces.put(number, cardNode);
                }
            }
            backFaces.forEach(frontFaces::putIfAbsent);
            splitHalves.values().forEach(halves -> halves.sort(Comparator.comparing(
                    half -> CardDataSupport.text(half, "side") == null ? "" : CardDataSupport.text(half, "side"))));
        }
        return new FaceIndex(frontFaces, backFaces, faceNames, splitHalves);
    }

    /**
     * MTGJSON's promo types, which use Scryfall's vocabulary, plus the one tag it never sets:
     * {@code planeswalkerdeck}. MTGJSON instead lists such a printing as found only in decks, so a
     * deck-only printing without promo types of its own is taken to be one. Commander and other
     * preconstructed-deck sets carry no booster types at all, so they do not match.
     */
    static List<String> promoTypes(JsonNode card) {
        List<String> promoTypes = CardDataSupport.strings(card, "promoTypes");
        if (promoTypes.isEmpty() && CardDataSupport.strings(card, "boosterTypes").contains("deck")) {
            return List.of("planeswalkerdeck");
        }
        return promoTypes;
    }

    private static Map<String, String> parseLegalities(JsonNode card) {
        Map<String, String> formats = new HashMap<>();
        JsonNode legalities = card.get("legalities");
        if (legalities != null) {
            legalities.properties().forEach(entry -> formats.put(
                    entry.getKey().toLowerCase(Locale.ROOT),
                    entry.getValue().asText().toLowerCase(Locale.ROOT)));
        }
        return formats;
    }

    static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    static HttpRequest createRequest(String setCode) {
        String url = "https://mtgjson.com/api/v5/" + setCode.toUpperCase(Locale.ROOT) + ".json";
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    /**
     * Flattens one MTGJSON card entry into a face. Close to a field rename — MTGJSON already splits
     * faces upstream, so each entry is self-contained — plus this source's two compensating quirks:
     * the loyalty-bracket syntax and the watermark suffix.
     */
    static RawFace toRawFace(JsonNode face) {
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
                face.has("faceName") && !"split".equals(CardDataSupport.text(face, "layout"))
                        ? face.get("faceName").asText() : CardDataSupport.text(face, "name"),
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
                watermark,
                CardDataSupport.text(face, "layout"),
                CardDataSupport.text(face, "manaValue"));
    }

    /**
     * A side-"b" entry as a back face. Named by its face name even on a split card, whose
     * {@link #toRawFace} is named after the whole card so that its front face can be.
     */
    private static RawFace toRawBackFace(JsonNode back) {
        RawFace face = toRawFace(back);
        String faceName = CardDataSupport.text(back, "faceName");
        return faceName == null ? face : new RawFace(
                faceName, face.manaCost(), face.typeLine(), face.text(), face.colors(),
                face.colorIndicator(), face.colorIdentity(), face.power(), face.toughness(),
                face.loyalty(), face.defense(), face.keywords(), face.watermark(), face.layout(),
                face.manaValue());
    }

    /** One side entry of a split card as a half, named by its own face name. */
    private static RawFace toRawSplitHalf(JsonNode half) {
        RawFace face = toRawFace(half);
        return new RawFace(
                CardDataSupport.text(half, "faceName"), face.manaCost(), face.typeLine(), face.text(),
                face.colors(), face.colorIndicator(), face.colorIdentity(), null, null, null, null,
                face.keywords(), null, face.layout(), null);
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
}
