package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered punisher effect (Prince of Thralls): a permanent an opponent controls was put into a
 * graveyard from the battlefield. That opponent may pay {@code lifeCost} life; if they don't (or
 * can't), the ability's controller puts that card onto the battlefield under their control, tracked
 * as a stolen permanent so it returns to its owner on death.
 *
 * <p>The no-arg-ish {@code (lifeCost)} constructor is used in the card definition; the collector
 * {@code DeathTriggerCollectorService} stamps the concrete {@code dyingCardId} and
 * {@code payingPlayerId} when the trigger fires.
 *
 * @param lifeCost       how much life "that opponent" may pay to keep the permanent from being stolen
 * @param dyingCardId    the card ID of the permanent put into the graveyard (null in the card definition)
 * @param payingPlayerId the opponent who controlled it, i.e. the player who may pay the life (null in
 *                       the card definition)
 */
public record StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost, UUID dyingCardId,
                                                              UUID payingPlayerId) implements ControlStealingEffect {

    public StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost) {
        this(lifeCost, null, null);
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
