package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.OracleData;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The single source of truth for what the game knows about each {@link CardSet}: the implemented
 * printings found by scanning the classpath, plus the set metadata the oracle data supplies.
 *
 * <p>Owns the startup sequence. It scans first, then hands itself to the {@link OracleLoader} to be
 * filled — the loader needs the printings to know which card classes to register oracle data for,
 * so the ordering is explicit here rather than emerging from whichever static happened to be
 * touched first.
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

    private final Map<String, String> setNames = new ConcurrentHashMap<>();
    private final Map<String, Integer> setCardTotals = new ConcurrentHashMap<>();
    private volatile Map<CardSet, List<CardPrinting>> printings = Map.of();

    public CardRegistry(OracleLoader loader) {
        this.loader = loader;
    }

    @PostConstruct
    void load() {
        printings = CardScanner.scan();

        for (CardSet cardSet : CardSet.values()) {
            List<CardPrinting> setPrintings = getPrintings(cardSet);
            Set<String> implemented = setPrintings.stream()
                    .map(CardPrinting::collectorNumber)
                    .collect(Collectors.toSet());

            register(cardSet, setPrintings, loader.loadSet(cardSet.getCode(), implemented));
        }

        LOG.info("Oracle registry populated for all card sets");
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

            // A temp card supplies the class name the oracle data is keyed by, and whether this
            // printing has a back face at all.
            Card tempCard = printing.factory().get();
            Card.registerOracle(tempCard.getClass().getSimpleName(), front);

            String backFaceClassName = tempCard.getBackFaceClassName();
            if (backFaceClassName != null) {
                OracleData back = data.backFaceByCollectorNumber().get(printing.collectorNumber());
                if (back != null) {
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
        return getPrintings(set).stream()
                .filter(printing -> printing.collectorNumber().equals(collectorNumber))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No printing with collector number " + collectorNumber + " in set " + set.getCode()));
    }

    @Override
    public String getName(CardSet set) {
        return setNames.getOrDefault(set.getCode(), set.getCode());
    }

    @Override
    public int getSetCardTotal(CardSet set) {
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
