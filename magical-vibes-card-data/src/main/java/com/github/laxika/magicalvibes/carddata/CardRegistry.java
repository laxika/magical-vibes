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

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
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

        if (loadMode == OracleLoadMode.ON_DEMAND) {
            indexBackFaces();
            Card.installOracleDataResolver(oracleDataResolver);
            LOG.info("Card registry ready for on-demand oracle loading");
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

        CardSet cardSet = preferredSet(cardClass);
        if (cardSet == null) {
            cardSet = backFaceSets.get(cardClass);
        }
        if (cardSet == null) {
            // Synthetic Card subclasses are common in engine tests and intentionally have no data.
            return;
        }
        ensureSetLoaded(cardSet);
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

            Card.registerOracle(printing.simpleCardClassName(), front);

            if (loadMode == OracleLoadMode.EAGER || printing.hasBackFace()) {
                Card tempCard = constructForRegistration(printing);
                String backFaceClassName = tempCard.getBackFaceClassName();
                OracleData back = data.backFaceByCollectorNumber().get(printing.collectorNumber());
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
