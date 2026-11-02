package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Which of the old life gain, loss and payment slot collectors reads a triggered ability, now that the ability has a
 * real trigger (effect-slot-refactor.md step 13, family 9). Each constant is named after the slot whose collector it
 * stands for and names the trigger its cards were converted to and the zone the collector reads them in. The
 * collectors in {@code TriggerCollectionService} still decide when the ability triggers, how its effects group into
 * stack objects, which wrappers filter it and which event data it is bound to, as the slot's did, until steps 14 and
 * 15 of the refactor replace them with trigger options and effects that read the event.
 */
public enum LifeWatch implements TriggerWatch {
    /** "Whenever an opponent loses life", damage included, once for each life-loss event. */
    ON_OPPONENT_LOSES_LIFE(Triggers.opponentLosesLife(), Zone.BATTLEFIELD),
    /** "Whenever a player gains life"; only read on temporary global abilities (False Cure). */
    ON_ANY_PLAYER_GAINS_LIFE(Triggers.playerGainsLife(), Zone.BATTLEFIELD),
    /** "Whenever you gain life". */
    ON_CONTROLLER_GAINS_LIFE(Triggers.youGainLife(), Zone.BATTLEFIELD),
    /** "Whenever an opponent gains life". */
    ON_OPPONENT_GAINS_LIFE(Triggers.opponentGainsLife(), Zone.BATTLEFIELD),
    /** "Whenever you lose life", damage included, once for each life-loss event. */
    ON_CONTROLLER_LOSES_LIFE(Triggers.youLoseLife(), Zone.BATTLEFIELD),
    /** "Whenever you pay life". */
    ON_CONTROLLER_PAYS_LIFE(Triggers.youPayLife(), Zone.BATTLEFIELD),
    /** "Whenever an opponent gains life", while this card is in your graveyard. */
    GRAVEYARD_ON_OPPONENT_GAINS_LIFE(Triggers.opponentGainsLife(), Zone.GRAVEYARD),
    /** "Whenever you gain life", while this card is in your graveyard. */
    GRAVEYARD_ON_CONTROLLER_GAINS_LIFE(Triggers.youGainLife(), Zone.GRAVEYARD);

    private final Trigger trigger;
    private final Zone zone;

    LifeWatch(Trigger trigger, Zone zone) {
        this.trigger = trigger;
        this.zone = zone;
    }

    @Override
    public Trigger trigger() {
        return trigger;
    }

    @Override
    public Zone zone() {
        return zone;
    }
}
