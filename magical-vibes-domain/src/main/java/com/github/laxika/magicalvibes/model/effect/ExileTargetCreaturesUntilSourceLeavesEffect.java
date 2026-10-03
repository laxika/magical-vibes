package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may exile up to {@code maxTargets} other target creatures from the battlefield and/or
 * creature cards from graveyards. When this creature leaves the battlefield, return the exiled
 * cards to their owners' hands." (Angel of Serenity)
 *
 * <p>The controller may choose zero targets; {@code optionalAtResolution} separately permits
 * declining exile after choosing targets. The source permanent itself is never a legal choice.
 * Each exiled card is registered as a pending return keyed on the source permanent;
 * {@code returnToHand} picks the return zone.
 *
 * <p>Tokens exiled this way cease to exist (CR 111.7) and register no return.
 */
public record ExileTargetCreaturesUntilSourceLeavesEffect(int maxTargets, boolean returnToHand,
                                                          boolean xScaled, boolean optionalAtResolution)
        implements CardEffect, BattlefieldAndGraveyardCardChoosingEffect {

    public ExileTargetCreaturesUntilSourceLeavesEffect(int maxTargets, boolean returnToHand, boolean xScaled) {
        this(maxTargets, returnToHand, xScaled, false);
    }

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
