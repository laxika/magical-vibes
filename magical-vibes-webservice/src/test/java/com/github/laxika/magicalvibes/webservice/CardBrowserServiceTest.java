package com.github.laxika.magicalvibes.webservice;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.carddata.OracleDataProvider;
import com.github.laxika.magicalvibes.carddata.RawFace;
import com.github.laxika.magicalvibes.carddata.store.PrintVariant;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.SetSource;
import com.github.laxika.magicalvibes.carddata.store.StoredPrinting;
import com.github.laxika.magicalvibes.carddata.store.StoredSet;
import com.github.laxika.magicalvibes.networking.message.BrowseCardInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CardBrowserServiceTest {

    private static final PrintVariant BASE = new PrintVariant(List.of(), List.of(), "black", false);

    @TempDir
    Path cacheDir;

    @Test
    void listsBasePrintingsAndSkipsVariantsAndPromos() {
        List<BrowseCardInfo> cards = service().buildCards("M19", set(
                printing("1", BASE),
                printing("2a", BASE),
                printing("3s", BASE),
                printing("4★", BASE),
                printing("300", new PrintVariant(List.of(), List.of("showcase"), "black", false)),
                printing("301", new PrintVariant(List.of(), List.of("extendedart"), "black", false)),
                printing("302", new PrintVariant(List.of(), List.of(), "borderless", false)),
                printing("303", new PrintVariant(List.of(), List.of(), "black", true)),
                printing("304", new PrintVariant(List.of("buyabox"), List.of(), "black", false))));

        assertThat(cards).extracting(BrowseCardInfo::collectorNumber).containsExactly("1", "2a");
    }

    /** Numbered extras of the set itself, shown under their own deck-builder sections. */
    @Test
    void keepsPlaneswalkerDeckAndSetExtensionPrintingsEvenWithAShowcaseFrame() {
        List<BrowseCardInfo> cards = service().buildCards("M19", set(
                printing("281", new PrintVariant(List.of("planeswalkerdeck"), List.of(), "black", false)),
                printing("250", new PrintVariant(List.of("setextension", "boosterfun"), List.of("showcase"),
                        "borderless", false))));

        assertThat(cards).extracting(BrowseCardInfo::collectorNumber).containsExactly("281", "250");
        assertThat(cards.getFirst().promoTypes()).containsExactly("planeswalkerdeck");
    }

    @Test
    void mapsTheFrontFaceThroughTheOracleMapper() {
        RawFace front = new RawFace("Llanowar Elves", "{G}", "Creature — Elf Druid",
                "{T}: Add {G}.", List.of("G"), List.of(), List.of("G"), "1", "1", null, null,
                List.of(), null);

        BrowseCardInfo card = service().buildCards("M19", set(new StoredPrinting("314", "common",
                List.of("Llanowar Elves"), front, null, List.of(), BASE, Map.of()))).getFirst();

        assertThat(card.name()).isEqualTo("Llanowar Elves");
        assertThat(card.manaCost()).isEqualTo("{G}");
        assertThat(card.typeLine()).isEqualTo("Creature — Elf Druid");
        assertThat(card.type()).isEqualTo("CREATURE");
        assertThat(card.subtypes()).containsExactly("ELF", "DRUID");
        assertThat(card.color()).isEqualTo("GREEN");
        assertThat(card.colors()).containsExactly("GREEN");
        assertThat(card.cardText()).isEqualTo("{T}: Add {G}.");
        assertThat(card.power()).isEqualTo("1");
        assertThat(card.rarity()).isEqualTo("common");
        assertThat(card.backFace()).isNull();
    }

    @Test
    void tintsAColorlessLandByItsColorIdentity() {
        RawFace forest = new RawFace("Forest", null, "Basic Land — Forest", null, List.of(), List.of(),
                List.of("G"), null, null, null, null, List.of(), null);

        BrowseCardInfo card = service().buildCards("M19", set(new StoredPrinting("280", "common",
                List.of("Forest"), forest, null, List.of(), BASE, Map.of()))).getFirst();

        assertThat(card.colors()).isEmpty();
        assertThat(card.color()).isEqualTo("GREEN");
        assertThat(card.supertypes()).containsExactly("BASIC");
    }

    @Test
    void offersADoubleFacedCardsSecondFaceAsItsBackFace() {
        RawFace front = new RawFace("Huntmaster of the Fells // Ravager of the Fells", "{2}{R}{G}",
                "Creature — Human Werewolf", "Trample", List.of("G", "R"), List.of(), List.of("G", "R"),
                "2", "2", null, null, List.of("Trample", "Transform"), null, "transform", null);
        RawFace back = new RawFace("Ravager of the Fells", null, "Creature — Werewolf", "Trample",
                List.of(), List.of("G", "R"), List.of("G", "R"), "4", "4", null, null,
                List.of("Trample", "Transform"), null, "transform", null);

        BrowseCardInfo card = service().buildCards("M19", set(new StoredPrinting("140", "mythic",
                List.of(), front, back, List.of(), BASE, Map.of()))).getFirst();

        assertThat(card.name()).isEqualTo("Huntmaster of the Fells");
        assertThat(card.prepareSpell()).isNull();
        assertThat(card.backFace().name()).isEqualTo("Ravager of the Fells");
        assertThat(card.backFace().colors()).containsExactly("GREEN", "RED");
        assertThat(card.backFace().power()).isEqualTo("4");
    }

    /** The spell is printed inset on a prepare card's front, so it must not get a flip control. */
    @Test
    void handsAPrepareCardsSpellBackAsItsPrepareSpell() {
        RawFace front = new RawFace("Abigale, Poet Laureate // Heroic Stanza", "{1}{W}{B}",
                "Legendary Creature — Bird Bard", "Flying", List.of("B", "W"), List.of(), List.of("B", "W"),
                "2", "3", null, null, List.of("Flying"), null, "prepare", null);
        RawFace spell = new RawFace("Heroic Stanza", "{1}{W/B}", "Sorcery",
                "Put a +1/+1 counter on target creature.", List.of(), List.of(), List.of("B", "W"),
                null, null, null, null, List.of("Flying"), null, "prepare", null);

        BrowseCardInfo card = service().buildCards("SOS", set(new StoredPrinting("1", "rare",
                List.of(), front, spell, List.of(), BASE, Map.of()))).getFirst();

        assertThat(card.backFace()).isNull();
        assertThat(card.prepareSpell().name()).isEqualTo("Heroic Stanza");
    }

    @Test
    void marksTheImplementedPrintings() {
        List<BrowseCardInfo> cards = service(List.of(new CardPrinting("M19", "2", "Card", "Card", false, null)))
                .buildCards("M19", set(printing("1", BASE), printing("2", BASE)));

        assertThat(cards).extracting(BrowseCardInfo::implemented).containsExactly(false, true);
    }

    /** A failed load must not stick: once the provider is back, the set has to show up. */
    @Test
    void aSetThatCouldNotBeLoadedIsRetriedOnTheNextRequest() {
        List<Boolean> reachable = new ArrayList<>(List.of(false));
        SetSource source = new SetSource() {
            @Override
            public OracleDataProvider provider() {
                return OracleDataProvider.MTGJSON;
            }

            @Override
            public StoredSet download(String setCode, Path tempDir) throws IOException {
                if (!reachable.getLast()) {
                    throw new IOException("MTGJSON is down");
                }
                return set(printing("1", BASE));
            }
        };
        CardBrowserService service = new CardBrowserService(catalog(List.of()),
                new SetDataStore(cacheDir.toString(), "MTGJSON", List.of(source)));

        assertThat(service.getCardsForSet("M19")).isEmpty();
        reachable.add(true);
        assertThat(service.getCardsForSet("M19")).extracting(BrowseCardInfo::collectorNumber).containsExactly("1");
    }

    private static CardBrowserService service() {
        return service(List.of());
    }

    private static CardBrowserService service(List<CardPrinting> implemented) {
        return new CardBrowserService(catalog(implemented), null);
    }

    private static CardCatalog catalog(List<CardPrinting> implemented) {
        return new CardCatalog() {
            @Override
            public List<CardPrinting> getPrintings(CardSet set) {
                return implemented;
            }

            @Override
            public CardPrinting findByCollectorNumber(CardSet set, String collectorNumber) {
                return null;
            }

            @Override
            public String getName(CardSet set) {
                return null;
            }

            @Override
            public int getSetCardTotal(CardSet set) {
                return 0;
            }

            @Override
            public double getImplementedFraction(CardSet set) {
                return 0;
            }
        };
    }

    private static StoredSet set(StoredPrinting... printings) {
        return new StoredSet(StoredSet.FORMAT_VERSION, "M19", "Test Set", OracleDataProvider.MTGJSON, null,
                Instant.EPOCH, "tm19", List.of(printings), List.of());
    }

    private static StoredPrinting printing(String number, PrintVariant variant) {
        RawFace face = new RawFace("Grizzly Bears", "{1}{G}", "Creature — Bear", null, List.of("G"),
                List.of(), List.of("G"), "2", "2", null, null, List.of(), null);
        return new StoredPrinting(number, "common", List.of("Grizzly Bears"), face, null, List.of(), variant, Map.of());
    }
}
