package com.github.laxika.magicalvibes.carddata.scryfall;

import com.github.laxika.magicalvibes.carddata.CardDataSupport;
import com.github.laxika.magicalvibes.carddata.FaceOracleMapper;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.SetSource;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.carddata.store.StoredToken;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Downloads a set from the Scryfall API: the paginated card search for the set itself, plus its
 * separate {@code t}-prefixed token set.
 *
 * <p>The conversion helpers stay {@code static} — they are pure functions over a {@code JsonNode}
 * and the unit tests call them directly, without a container.
 */
@Component
public class ScryfallSetSource implements SetSource {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    /**
     * Every card object of one Scryfall set search, or an empty list when Scryfall has no such
     * set. Owns its own rate limiting.
     */
    @FunctionalInterface
    interface SetFetcher {
        List<JsonNode> fetch(String scryfallSetCode, Path tempDir) throws IOException, InterruptedException;
    }

    private final SetFetcher fetcher;

    @Autowired
    public ScryfallSetSource() {
        this(ScryfallSetSource::fetchFromScryfall);
    }

    ScryfallSetSource(SetFetcher fetcher) {
        this.fetcher = fetcher;
    }

    @Override
    public OracleDataProvider provider() {
        return OracleDataProvider.SCRYFALL;
    }

    /**
     * A set without a token set is normal, so an empty token search is not a failure; any other
     * token-set failure fails the download like a card-set failure would.
     */
    @Override
    public StoredSet download(String setCode, Path tempDir) throws IOException, InterruptedException {
        String sourceSetCode = "MB1".equalsIgnoreCase(setCode) ? "CMB1" : setCode;
        List<JsonNode> cards = fetcher.fetch(sourceSetCode, tempDir);
        List<JsonNode> tokens = fetcher.fetch(tokenSetCode(sourceSetCode), tempDir);
        return convert(setCode, sourceSetCode, cards, tokens, Instant.now());
    }

    static StoredSet convert(String setCode, String sourceSetCode, List<JsonNode> cards,
                             List<JsonNode> tokenCards, Instant fetchedAt) {
        String setName = null;
        Map<String, StoredPrinting> printings = new LinkedHashMap<>();
        for (JsonNode card : cards) {
            if (setName == null) {
                setName = CardDataSupport.text(card, "set_name");
            }
            StoredPrinting printing = toPrinting(card);
            printings.put(printing.collectorNumber(), printing);
        }

        List<StoredToken> tokens = new ArrayList<>();
        for (JsonNode token : tokenCards) {
            tokens.add(new StoredToken(CardDataSupport.text(token, "collector_number"), null,
                    toRawFrontFace(token)));
        }

        return new StoredSet(StoredSet.FORMAT_VERSION, setCode, setName, OracleDataProvider.SCRYFALL,
                null, fetchedAt, tokenSetCode(sourceSetCode), List.copyOf(printings.values()), tokens);
    }

    private static String tokenSetCode(String sourceSetCode) {
        return "t" + sourceSetCode.toLowerCase(Locale.ROOT);
    }

    private static List<JsonNode> fetchFromScryfall(String setCode, Path tempDir)
            throws IOException, InterruptedException {
        List<JsonNode> allCards = new ArrayList<>();
        // include_variations=true pulls lettered printings that are variations of a base card
        // (Portal demo-game 6d/69d, etc.). Without it Scryfall omits them from set searches.
        String url = "https://api.scryfall.com/cards/search?q=set:" + setCode.toLowerCase(Locale.ROOT)
                + "&unique=prints&include_variations=true";

        try (HttpClient client = HttpClient.newHttpClient()) {
            for (int page = 1; url != null; page++) {
                // Respect Scryfall rate limits: 50-100ms between requests
                Thread.sleep(100);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", "MagicalVibes/1.0")
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                Path pageFile = tempDir.resolve("scryfall-" + setCode.toLowerCase(Locale.ROOT)
                        + "-" + page + ".json");
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(pageFile));

                // A search matching nothing is a 404: the set does not exist on Scryfall.
                if (response.statusCode() == 404 && page == 1) {
                    return List.of();
                }
                if (response.statusCode() != 200) {
                    throw new IOException("Scryfall API returned " + response.statusCode() + " for set " + setCode);
                }

                JsonNode body = MAPPER.readTree(Files.readString(pageFile));
                JsonNode data = body.get("data");
                if (data == null || !data.isArray()) {
                    throw new IOException("Scryfall page " + page + " for set " + setCode + " has no data array");
                }
                for (JsonNode card : data) {
                    allCards.add(card);
                }

                url = body.has("has_more") && body.get("has_more").asBoolean()
                        ? body.get("next_page").asText()
                        : null;
            }
        }
        return allCards;
    }

    static StoredPrinting toPrinting(JsonNode card) {
        RawFace front = toRawFrontFace(card);
        return new StoredPrinting(
                CardDataSupport.text(card, "collector_number"),
                CardDataSupport.text(card, "rarity"),
                parseFaceNames(card),
                front,
                toRawBackFace(card),
                FaceOracleMapper.isSplitLayout(front.layout()) ? toRawSplitHalves(card) : List.of(),
                new PrintVariant(
                        CardDataSupport.strings(card, "promo_types"),
                        CardDataSupport.strings(card, "frame_effects"),
                        CardDataSupport.text(card, "border_color"),
                        card.has("full_art") && card.get("full_art").asBoolean()),
                parseLegalities(card));
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

    /**
     * Each {@code card_faces} entry of a split card as a half. Scryfall keeps a half's name, cost,
     * type line and text on the face but its colours (combined) and keywords on the card.
     */
    private static List<RawFace> toRawSplitHalves(JsonNode card) {
        JsonNode faces = card.get("card_faces");
        if (faces == null || !faces.isArray()) {
            return List.of();
        }
        List<RawFace> halves = new ArrayList<>();
        for (JsonNode face : faces) {
            halves.add(new RawFace(
                    CardDataSupport.text(face, "name"),
                    CardDataSupport.text(face, "mana_cost"),
                    CardDataSupport.text(face, "type_line"),
                    CardDataSupport.text(face, "oracle_text"),
                    List.of(),
                    List.of(),
                    CardDataSupport.strings(card, "color_identity"),
                    null,
                    null,
                    null,
                    null,
                    CardDataSupport.strings(card, "keywords"),
                    null,
                    CardDataSupport.text(card, "layout"),
                    null));
        }
        return halves;
    }

    private static List<String> parseFaceNames(JsonNode card) {
        List<String> names = new ArrayList<>();
        JsonNode faces = card.get("card_faces");
        if (faces != null && faces.isArray()) {
            for (JsonNode face : faces) {
                if (face.has("name")) {
                    names.add(face.get("name").asText());
                }
            }
        }
        if (names.isEmpty() && card.has("name")) {
            names.add(card.get("name").asText());
        }
        return List.copyOf(names);
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
                // Top-level on a double-faced card, but only on the faces of a split card.
                CardDataSupport.text(prefer(face, card, "watermark"), "watermark"),
                CardDataSupport.text(card, "layout"),
                CardDataSupport.text(card, "cmc"));
    }

    private static JsonNode prefer(JsonNode face, JsonNode card, String field) {
        return face.has(field) ? face : card;
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
                CardDataSupport.text(card, "watermark"),
                CardDataSupport.text(card, "layout"),
                CardDataSupport.text(card, "cmc"));
    }

    /**
     * Returns card_faces[0] for layouts whose characteristics live on the face node,
     * including transform DFCs, split cards (including aftermath), and reversible cards, or the
     * card itself for normal cards.
     */
    private static JsonNode getFrontFaceNode(JsonNode card) {
        if (card.has("card_faces") && card.has("layout")) {
            String layout = card.get("layout").asText();
            if ("transform".equals(layout) || "split".equals(layout) || "flip".equals(layout)
                    || "adventure".equals(layout) || "modal_dfc".equals(layout)
                    || "prepare".equals(layout) || "reversible_card".equals(layout)) {
                return card.get("card_faces").get(0);
            }
        }
        return card;
    }
}
