package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.input.CardNameCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link CardNameCatalog} of the test context: while a test that declares its cards through
 * {@link CardUsed} runs, a "choose a card name" prompt offers those cards (and, as always, the
 * cards in the game) instead of every implemented card. Offering them all loads every card class
 * and every oracle set, which a test JVM otherwise never needs.
 *
 * <p>A test that names a card it neither puts into the game nor declares is therefore refused the
 * name; the fix is to declare the card.
 *
 * <p>Outside such a test — the harness is also used by tests that do not extend
 * {@link BaseCardTest} — every implemented printing is offered, as in the application.
 */
public final class DeclaredCardNameCatalog implements CardNameCatalog {

    /**
     * The card classes the running test declares, or null outside such a test. Static, not state
     * of the bean, so that declaring cards does not start the engine context.
     */
    private static volatile List<Class<? extends Card>> declared;

    private final CardCatalog cardCatalog;
    private volatile Map<String, CardPrinting> printingsByClassName;
    private volatile List<CardPrinting> allPrintings;
    private volatile Resolved resolved;

    /** The printings of one declaration, kept so that an unchanged declaration returns one list. */
    private record Resolved(List<Class<? extends Card>> declared, List<CardPrinting> printings) {
    }

    DeclaredCardNameCatalog(CardCatalog cardCatalog) {
        this.cardCatalog = cardCatalog;
    }

    /** Limits the offer to these card classes until the next declaration or {@link #clear()}. */
    static void declare(List<Class<? extends Card>> cardClasses) {
        if (!cardClasses.equals(declared)) {
            declared = List.copyOf(cardClasses);
        }
    }

    /** Lifts the limit: every implemented printing is offered again. */
    static void clear() {
        declared = null;
    }

    /** The card classes the offer is limited to, or null when it is not limited. */
    static List<Class<? extends Card>> declared() {
        return declared;
    }

    /**
     * A declared class without a registered printing of its own — a back face, a synthetic test
     * card — has no catalog name to offer and is skipped.
     */
    @Override
    public List<CardPrinting> printings() {
        List<Class<? extends Card>> limit = declared;
        if (limit == null) {
            return allPrintings();
        }
        Resolved current = resolved;
        if (current == null || current.declared() != limit) {
            List<CardPrinting> printings = new ArrayList<>();
            for (Class<? extends Card> cardClass : limit) {
                CardPrinting printing = printingsByClassName().get(cardClass.getName());
                if (printing != null) {
                    printings.add(printing);
                }
            }
            current = new Resolved(limit, List.copyOf(printings));
            resolved = current;
        }
        return current.printings();
    }

    private List<CardPrinting> allPrintings() {
        List<CardPrinting> cached = allPrintings;
        if (cached == null) {
            List<CardPrinting> all = new ArrayList<>();
            for (CardSet set : CardSet.values()) {
                all.addAll(cardCatalog.getPrintings(set));
            }
            cached = List.copyOf(all);
            allPrintings = cached;
        }
        return cached;
    }

    private Map<String, CardPrinting> printingsByClassName() {
        Map<String, CardPrinting> cached = printingsByClassName;
        if (cached == null) {
            Map<String, CardPrinting> index = new HashMap<>();
            for (CardPrinting printing : allPrintings()) {
                index.putIfAbsent(printing.cardClassName(), printing);
            }
            cached = index;
            printingsByClassName = cached;
        }
        return cached;
    }
}
