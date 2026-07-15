package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target player reveals {@code revealCount} cards from their hand and you choose {@code discardCount}
 * of them. That player discards those cards." (Blackmail — reveal 3, discard 1; Noggin Whack —
 * reveal 3, discard 2).
 *
 * <p>Unlike the Duress family ({@link ChooseCardsFromTargetHandEffect}), the target player picks
 * which cards to reveal — only those cards are shown to the controller, who then chooses which to
 * discard. If the target has {@code revealCount} or fewer cards, their whole hand is revealed with
 * no choice; the controller then discards up to {@code discardCount} of the revealed cards.
 */
public record TargetRevealsCardsControllerChoosesDiscardEffect(int revealCount, int discardCount)
        implements CardEffect {

    /** Convenience for the common "choose one" case (Blackmail). */
    public TargetRevealsCardsControllerChoosesDiscardEffect(int revealCount) {
        this(revealCount, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
