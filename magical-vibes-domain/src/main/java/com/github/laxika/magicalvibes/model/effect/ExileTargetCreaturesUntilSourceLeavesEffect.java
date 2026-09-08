package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may exile up to {@code maxTargets} other target creatures from the battlefield and/or
 * creature cards from graveyards. When this creature leaves the battlefield, return the exiled
 * cards to their owners' hands." (Angel of Serenity)
 *
 * <p>The "you may" is carried by the "up to" wording — the controller may choose zero targets. The
 * source permanent itself is never a legal choice ("other"). Each exiled card is registered as a
 * pending return keyed on the source permanent, so the leaves-the-battlefield half of the oracle
 * text is implicit (handled by {@code PermanentRemovalService}) exactly as for the O-ring family;
 * {@code returnToHand} picks the return zone (hand for Angel of Serenity, battlefield for an
 * O-ring style return).
 *
 * <p>Tokens exiled this way cease to exist (CR 111.7) and register no return.
 */
public record ExileTargetCreaturesUntilSourceLeavesEffect(int maxTargets, boolean returnToHand,
                                                          boolean xScaled)
        implements CardEffect, BattlefieldAndGraveyardCardChoosingEffect {

    public ExileTargetCreaturesUntilSourceLeavesEffect(int maxTargets, boolean returnToHand) {
        this(maxTargets, returnToHand, false);
    }

    @Override
    public int mixedZoneMaxTargets() {
        return maxTargets;
    }

    @Override
    public int mixedZoneMaxTargets(int xValue) {
        return xScaled ? Math.max(0, xValue) : maxTargets;
    }
}
