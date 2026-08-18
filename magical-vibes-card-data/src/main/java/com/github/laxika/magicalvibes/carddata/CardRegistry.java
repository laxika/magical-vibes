package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OracleData;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The single source of truth for what the game knows about each {@link CardSet}: the implemented
 * printings found by scanning the classpath, plus the set metadata the oracle data supplies.
 *
 * <p>Owns the loading sequence. It scans first, then either loads every set during startup or lets
 * the test context request sets on demand. The loader needs the printings to know which card
 * classes to parse oracle data for, so the ordering is explicit here rather than emerging from
 * whichever static happened to be touched first.
 *
 * <p>Taking the loader as a required constructor dependency is also what makes a misconfigured
 * {@code oracle.data-provider} fail loudly: no loader bean means no registry bean, and Spring
 * refuses to start the context. Previously nothing injected the loader, so a property naming no
 * provider booted an application with an empty oracle registry.
 */
@Service
public class CardRegistry implements CardCatalog {

    private static final Logger LOG = Logger.getLogger(CardRegistry.class.getName());
    private static final Pattern NON_IDENTIFIER = Pattern.compile("[^a-z0-9]");

    private final OracleLoader loader;
    private final OracleLoadMode loadMode;
    private final Card.OracleDataResolver oracleDataResolver = this::resolveMissingOracleData;
    private final ThreadLocal<Boolean> suppressOracleResolution = ThreadLocal.withInitial(() -> false);

    private final Map<String, String> setNames = new ConcurrentHashMap<>();
    private final Map<String, Integer> setCardTotals = new ConcurrentHashMap<>();
    private final Set<CardSet> loadedSets = EnumSet.noneOf(CardSet.class);
    private volatile Map<CardSet, List<CardPrinting>> printings = Map.of();
    private volatile Map<Class<? extends Card>, CardSet> backFaceSets = Map.of();

    public CardRegistry(OracleLoader loader) {
        this(loader, OracleLoadMode.EAGER);
    }

    @Autowired
    public CardRegistry(
            OracleLoader loader,
            @Value("${oracle.data-load-mode:EAGER}") OracleLoadMode loadMode) {
        this.loader = loader;
        this.loadMode = loadMode;
    }

    @PostConstruct
    void load() {
        printings = CardScanner.scan();

        if (loadMode != OracleLoadMode.EAGER) {
            indexBackFaces();
            if (loadMode == OracleLoadMode.ON_DEMAND) {
                Card.installOracleDataResolver(oracleDataResolver);
            }
            LOG.info("Card registry ready for " + loadMode.name().toLowerCase(Locale.ROOT) + " oracle loading");
            return;
        }

        for (CardSet cardSet : CardSet.values()) {
            ensureSetLoaded(cardSet);
        }
        LOG.info("Oracle registry populated for all card sets");
    }

    @PreDestroy
    void close() {
        Card.uninstallOracleDataResolver(oracleDataResolver);
    }

    /** Loads and registers one set at most once. A failed load remains retryable. */
    public synchronized void ensureSetLoaded(CardSet cardSet) {
        if (loadedSets.contains(cardSet)) {
            return;
        }

        List<CardPrinting> setPrintings = getPrintings(cardSet);
        Set<String> implemented = setPrintings.stream()
                .map(CardPrinting::collectorNumber)
                .collect(Collectors.toSet());
        register(cardSet, setPrintings, loader.loadSet(cardSet.getCode(), implemented));
        loadedSets.add(cardSet);
    }

    /**
     * Loads the preferred oracle set for a registered card class. Test infrastructure uses this to
     * preload data declared by a test before that test constructs any cards.
     *
     * @throws IllegalArgumentException if the class is neither a registered front face nor an
     *                                  indexed back face
     */
    public void ensureCardDataLoaded(Class<? extends Card> cardClass) {
        CardSet cardSet = findSetFor(cardClass);
        if (cardSet == null) {
            throw new IllegalArgumentException("No registered printing found for " + cardClass.getName());
        }
        ensureSetLoaded(cardSet);
    }

    private void indexBackFaces() {
        Map<Class<? extends Card>, CardSet> backFaces = new HashMap<>();
        Set<String> inspectedFronts = new HashSet<>();

        for (CardSet cardSet : CardSet.values()) {
            for (CardPrinting printing : getPrintings(cardSet)) {
                if (!printing.hasBackFace() || !inspectedFronts.add(printing.cardClassName())) {
                    continue;
                }
                Card front = constructForRegistration(printing);
                Card back = front.getBackFaceCard();
                if (back != null) {
                    // Matches registerOracleIfAbsent: a back-face-only class's first set wins.
                    backFaces.putIfAbsent(back.getClass().asSubclass(Card.class), cardSet);
                }
            }
        }

        backFaceSets = Map.copyOf(backFaces);
    }

    private void resolveMissingOracleData(Class<? extends Card> cardClass) {
        if (loadMode != OracleLoadMode.ON_DEMAND || suppressOracleResolution.get()) {
            return;
        }

        CardSet cardSet = findSetFor(cardClass);
        if (cardSet == null) {
            // Synthetic Card subclasses are common in engine tests and intentionally have no data.
            return;
        }
        ensureSetLoaded(cardSet);
    }

    private CardSet findSetFor(Class<? extends Card> cardClass) {
        CardSet cardSet = preferredSet(cardClass);
        return cardSet != null ? cardSet : backFaceSets.get(cardClass);
    }

    private static CardSet preferredSet(Class<? extends Card> cardClass) {
        return Arrays.stream(cardClass.getAnnotationsByType(CardRegistration.class))
                .map(registration -> CardSet.findByCode(registration.set()))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(CardSet::ordinal))
                .orElse(null);
    }

    private Card constructForRegistration(CardPrinting printing) {
        boolean wasSuppressed = suppressOracleResolution.get();
        suppressOracleResolution.set(true);
        try {
            return printing.factory().get();
        } finally {
            if (wasSuppressed) {
                suppressOracleResolution.set(true);
            } else {
                suppressOracleResolution.remove();
            }
        }
    }

    /**
     * Applies one set's loaded data to every registry it feeds. This loop used to exist once per
     * loader, hand-synced; it is provider-neutral, so it lives here now and each loader only parses.
     */
    private void register(CardSet cardSet, List<CardPrinting> setPrintings, SetOracleData data) {
        if (data.setName() != null) {
            registerSetName(cardSet.getCode(), data.setName());
        }
        registerSetCardTotal(cardSet.getCode(), data.cardTotal());

        data.rarityByCollectorNumber().forEach((collectorNumber, rarity) ->
                CardPrintingRegistry.registerRarity(cardSet.getCode(), collectorNumber, rarity));

        for (CardPrinting printing : setPrintings) {
            OracleData front = data.frontFaceByCollectorNumber().get(printing.collectorNumber());
            if (front == null) {
                LOG.warning("No oracle data for " + cardSet.getCode() + " #" + printing.collectorNumber());
                continue;
            }

            OracleData back = data.backFaceByCollectorNumber().get(printing.collectorNumber());
            verifyOracleNameMatchesClass(cardSet, printing, front, back);
            Card.registerOracle(printing.simpleCardClassName(), front);

            if (loadMode == OracleLoadMode.EAGER || printing.hasBackFace()) {
                Card tempCard = constructForRegistration(printing);
                String backFaceClassName = tempCard.getBackFaceClassName();
                if (backFaceClassName != null && back != null) {
                    // If-absent: the back face may name a standalone card class (prepare spells
                    // reuse the real spell class), whose own printing registers richer data that
                    // must win regardless of set load order.
                    Card.registerOracleIfAbsent(backFaceClassName, back);
                }
            }
        }

        if (!data.tokenImages().isEmpty()) {
            CardPrintingRegistry.registerTokenImages(cardSet.getCode(), data.tokenImages());
        }
    }

    /**
     * Rejects oracle data that arrived for a card class under another card's name.
     *
     * <p>A {@code @CardRegistration} collector number is typed by hand, and a wrong one is invisible
     * at every layer below this: the class keeps its own engine logic but silently takes on some
     * other card's name, cost, type and text, and the printing it was meant to claim is simply
     * never implemented. Disperse carried M15 #82 for a while, which is Void Snare — whichever of
     * its five printings loaded last decided what Disperse claimed to be, and the only symptom was
     * an unrelated test failing intermittently. Checking the loaded name against the class name
     * turns that whole class of typo into a set-load failure at the registration that caused it.
     *
     * <p>The comparison ignores case and every character a Java identifier cannot hold. Beyond that
     * it accepts only the spellings the card classes actually use, all of which exist in numbers:
     * an accented letter folded onto its base letter (Séance → {@code Seance}) or dropped outright
     * (Dandân → {@code DandN}); a legendary named by the part before the comma (Slimefoot, the
     * Stowaway → {@code Slimefoot}); and a double-faced card named after its front face alone
     * ({@code JaceVrynsProdigy}) or after both faces ({@code LoyalCatharUnhallowedCathar}).
     */
    private static void verifyOracleNameMatchesClass(
            CardSet cardSet, CardPrinting printing, OracleData front, OracleData back) {
        String backName = back == null ? null : back.name();
        if (front.name() == null
                || matchesClassName(printing.simpleCardClassName(), front.name(), backName)) {
            return;
        }
        throw new IllegalStateException(cardSet.getCode() + " #" + printing.collectorNumber()
                + " is \"" + front.name() + "\", but that printing is registered on "
                + printing.cardClassName() + ". Correct the @CardRegistration collector number, or"
                + " rename the class if it is the class name that is wrong.");
    }

    static boolean matchesClassName(String simpleClassName, String frontName, String backName) {
        String actual = identifierChars(simpleClassName);

        for (String front : nameStems(frontName)) {
            if (readsAs(actual, front)) {
                return true;
            }
            for (String back : nameStems(backName)) {
                if (readsAs(actual, front + back)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A face's name, plus its part before the comma when it is a legendary's. */
    private static List<String> nameStems(String name) {
        if (name == null) {
            return List.of();
        }
        String beforeComma = name.split(",")[0];
        return name.equals(beforeComma) ? List.of(name) : List.of(name, beforeComma);
    }

    /** Whether a card name reduces to the same identifier characters as a class name already has. */
    private static boolean readsAs(String classNameChars, String cardName) {
        return classNameChars.equals(identifierChars(cardName))
                || classNameChars.equals(
                        identifierChars(Normalizer.normalize(cardName, Normalizer.Form.NFKD)));
    }

    /** Lowercases and drops everything a Java identifier cannot contain, accented letters included. */
    private static String identifierChars(String value) {
        return NON_IDENTIFIER.matcher(value.toLowerCase(Locale.ROOT)).replaceAll("");
    }

    @Override
    public List<CardPrinting> getPrintings(CardSet set) {
        return printings.getOrDefault(set, List.of());
    }

    @Override
    public CardPrinting findByCollectorNumber(CardSet set, String collectorNumber) {
        CardPrinting printing = getPrintings(set).stream()
                .filter(candidate -> candidate.collectorNumber().equals(collectorNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No printing with collector number " + collectorNumber + " in set " + set.getCode()));
        ensureSetLoaded(set);
        return printing;
    }

    @Override
    public String getName(CardSet set) {
        ensureSetLoaded(set);
        return setNames.getOrDefault(set.getCode(), set.getCode());
    }

    @Override
    public int getSetCardTotal(CardSet set) {
        ensureSetLoaded(set);
        return setCardTotals.getOrDefault(set.getCode(), 0);
    }

    @Override
    public double getImplementedFraction(CardSet set) {
        int total = getSetCardTotal(set);
        if (total <= 0) {
            return 0.0;
        }
        return Math.min(1.0, (double) getPrintings(set).size() / total);
    }

    /** Called by the loader with the set's full name from the oracle data. */
    public void registerSetName(String setCode, String name) {
        setNames.put(setCode, name);
    }

    /**
     * Called by the loader with how many cards the set contains upstream — the denominator of
     * {@link #getImplementedFraction}, which is otherwise unknowable from the classpath scan.
     */
    public void registerSetCardTotal(String setCode, int total) {
        setCardTotals.put(setCode, total);
    }
}
