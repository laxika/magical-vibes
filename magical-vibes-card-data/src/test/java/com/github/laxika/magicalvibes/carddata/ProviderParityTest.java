package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonSetSource;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallSetSource;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.SetSource;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Holds the two sources to storing the same set. A set falls back to the other provider when its
 * preferred one is down on the first fetch, so anything the two disagree on would make a set's
 * cards depend on which provider happened to be reachable that day.
 *
 * <p>Compares what the engine and the card browser read: each printing's mapped faces, rarity and
 * face names, and whether the browser counts it as a variant. The sets cover a transform set, split
 * and aftermath cards, MB1's CMB1 data, and planeswalker-deck printings, which MTGJSON tags
 * differently. Each source stores into its own subdirectory of the shared test cache, so the sets
 * are downloaded once.
 */
@Disabled("Fetches sets from the Scryfall API: run by hand when needed, to spare Scryfall")
@Tag("scryfall")
@Tag("scryfall-api")
class ProviderParityTest {

    @ParameterizedTest
    @ValueSource(strings = {"ISD", "DGM", "AKH", "MB1", "M19"})
    void bothProvidersStoreTheSameSet(String setCode) throws Exception {
        Map<String, StoredPrinting> scryfall = printings(new ScryfallSetSource(), setCode);
        Map<String, StoredPrinting> mtgjson = printings(new MtgjsonSetSource(), setCode);

        assertThat(mtgjson.keySet()).as("collector numbers").isEqualTo(scryfall.keySet());
        List<String> differences = new ArrayList<>();
        for (String number : scryfall.keySet()) {
            compare(differences, number, "front", scryfall.get(number), mtgjson.get(number),
                    printing -> comparable(StoredSetOracleLoader.frontFace(printing)));
            compare(differences, number, "back", scryfall.get(number), mtgjson.get(number),
                    printing -> comparable(StoredSetOracleLoader.backFace(printing)));
            compare(differences, number, "rarity", scryfall.get(number), mtgjson.get(number),
                    StoredPrinting::rarity);
            compare(differences, number, "face names", scryfall.get(number), mtgjson.get(number),
                    StoredPrinting::faceNames);
            compare(differences, number, "browser variant", scryfall.get(number), mtgjson.get(number),
                    ProviderParityTest::browserVariant);
        }
        assertThat(differences).as("printings the providers store differently").isEmpty();
    }

    private static <T> void compare(List<String> differences, String number, String what,
                                    StoredPrinting scryfall, StoredPrinting mtgjson,
                                    Function<StoredPrinting, T> field) {
        T expected = field.apply(scryfall);
        T actual = field.apply(mtgjson);
        if (!java.util.Objects.equals(expected, actual)) {
            differences.add("#%s %s: Scryfall %s, MTGJSON %s".formatted(number, what, expected, actual));
        }
    }

    /** Oracle data minus the colour identity, which only tints a land's frame. */
    private static OracleData comparable(OracleData data) {
        return data == null ? null : new OracleData(data.name(), data.type(), data.additionalTypes(),
                data.manaCost(), data.color(), data.colors(), List.of(), data.supertypes(), data.subtypes(),
                data.cardText(), data.power(), data.toughness(), data.keywords(), data.loyalty(), data.defense(),
                data.watermark(), data.combinedManaValue(), data.splitHalves().stream()
                .map(ProviderParityTest::comparable).toList());
    }

    /** What the card browser makes of a printing's variant: hidden, a deck section, or shown. */
    private static String browserVariant(StoredPrinting printing) {
        Set<String> promoTypes = Set.copyOf(printing.variant().promoTypes());
        if (promoTypes.contains("planeswalkerdeck")) return "planeswalker deck";
        if (promoTypes.contains("setextension")) return "set extension";
        boolean hidden = !promoTypes.isEmpty() || printing.variant().fullArt()
                || "borderless".equals(printing.variant().borderColor())
                || printing.variant().frameEffects().contains("showcase")
                || printing.variant().frameEffects().contains("extendedart");
        return hidden ? "hidden" : "shown";
    }

    private static Map<String, StoredPrinting> printings(SetSource source, String setCode) throws Exception {
        String cacheDir = Path.of(SharedCacheLoaders.CACHE_DIR)
                .resolve("only-" + source.provider().name().toLowerCase()).toString();
        StoredSet set = new SetDataStore(cacheDir, source.provider().name(), List.of(source)).get(setCode);
        assertThat(set.source()).isEqualTo(source.provider());
        return set.printings().stream().collect(Collectors.toMap(StoredPrinting::collectorNumber,
                printing -> printing, (first, second) -> second, java.util.LinkedHashMap::new));
    }
}
