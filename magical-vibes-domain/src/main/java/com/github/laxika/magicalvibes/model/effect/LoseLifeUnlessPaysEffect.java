package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher effect: a player loses N life unless they pay {M}.
 * Used by Isolation Cell and similar "pay mana or lose life" cards; the spells that trigger a cast trigger's
 * penalty are the trigger's filter.
 * The affected player chooses whether to pay or take the life loss. When
 * {@code controllerGainsLifeLost} is true, the source's controller also gains the same amount
 * when the penalty is applied.
 *
 * @param lifeLoss    how much life the player loses if they don't pay
 * @param payAmount   generic mana cost the player can pay to avoid life loss
 * @param controllerGainsLifeLost whether the source's controller gains the life lost when the
 *                                 payment is not made
 * @param targetsPlayer whether the affected player is chosen as a target rather than supplied by
 *                      the triggering event
 */
public record LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, boolean controllerGainsLifeLost,
                                        boolean targetsPlayer) implements CardEffect {

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount) {
        this(lifeLoss, payAmount, false, true);
    }

    public LoseLifeUnlessPaysEffect(int lifeLoss, int payAmount, boolean controllerGainsLifeLost) {
        this(lifeLoss, payAmount, controllerGainsLifeLost, true);
    }

    public static LoseLifeUnlessPaysEffect forTriggeringPlayer(int lifeLoss, int payAmount) {
        return new LoseLifeUnlessPaysEffect(lifeLoss, payAmount, false, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
