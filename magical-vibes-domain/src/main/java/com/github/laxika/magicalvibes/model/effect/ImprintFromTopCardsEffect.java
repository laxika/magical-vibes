package com.github.laxika.magicalvibes.model.effect;

/**
 * Imprint — Look at the top N cards of your library, exile one face down,
 * then put the rest on the bottom of your library. The remainder is randomized
 * when {@code randomizeRest} is true.
 * The exiled card is imprinted on the source permanent.
 */
public record ImprintFromTopCardsEffect(int count, boolean randomizeRest) implements CardEffect {

    public ImprintFromTopCardsEffect(int count) {
        this(count, false);
    }

    /**
     * Whether this is hideaway (CR 702.75a), the form that puts the rest on the bottom in a random order: the exiled
     * card may be looked at by whoever controls the permanent, so each later controller may look too (Howltooth
     * Hollow's 2022-04-29 ruling), unlike an imprint such as Clone Shell's.
     */
    public boolean hideaway() {
        return randomizeRest;
    }
}
