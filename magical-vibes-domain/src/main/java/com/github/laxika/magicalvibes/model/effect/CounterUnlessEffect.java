package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for the "counter target spell (or ability) unless its controller pays a
 * ransom" family. Lets the trigger-collection and may-ability choice-flow services recognise the
 * family and route on the KIND of ransom without listing every concrete counter-unless variant,
 * mirroring how {@link CounterSpellingEffect} marks plain counterspells.
 *
 * <p>Descriptive only: both facts are drawn from the record's existing components, never a score.
 * The pay/discard choice-flow orchestration itself stays in the engine services — this interface
 * only unifies how those services recognise the effect and read its kind/magnitude.
 */
public interface CounterUnlessEffect extends CardEffect {

    /** The kind of ransom the controller may pay to avoid the counter. */
    enum RansomKind {
        /** Pay a generic mana amount (e.g. Mana Leak, Power Sink). */
        PAY_MANA,
        /** Pay a life amount (e.g. Phyrexian Fleshgorger's ward). */
        PAY_LIFE,
        /** Discard a card (e.g. the "Ward—Discard a card" variant). */
        DISCARD_CARD,
        /** Discard the entire hand. */
        DISCARD_HAND,
        /** Sacrifice a permanent (e.g. Mishra, Tamer of Mak Fawa's ward). */
        SACRIFICE_PERMANENT
    }

    /** Which kind of ransom this effect demands. */
    RansomKind ransomKind();

    /**
     * The base magnitude of the ransom, as a components-derived fact: for {@link RansomKind#PAY_MANA}
     * the generic mana amount component (may be overridden at resolution when the effect uses an X or
     * dynamic amount); for {@link RansomKind#PAY_LIFE} a dynamic amount may be evaluated at
     * resolution, so the concrete effect may return 0; for {@link RansomKind#DISCARD_CARD} the number
     * of cards to discard (currently always 1), for {@link RansomKind#DISCARD_HAND} 0, or 1 for a
     * permanent sacrifice.
     */
    int ransomMagnitude();
}
