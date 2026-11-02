package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Which of the old counter slot collectors reads a triggered ability, now that the ability has a real trigger
 * (effect-slot-refactor.md step 13, family 5). Each constant is named after the slot whose collector it stands for and
 * names the trigger its cards were converted to and the zone the collector reads them in. The collectors in
 * {@code TriggerCollectionService}, {@code PermanentCounterSupport} and the counter effect handlers still decide when
 * the ability triggers, how its effects group into stack objects and which wrappers filter it, and its
 * {@link CollectsCounterTrigger} handlers bind the event data, as the slot's did, until steps 14 and 15 of the refactor
 * replace them with trigger options and effects that read the event.
 */
public enum CounterWatch implements TriggerWatch {
    /** "Whenever a time counter is removed from this card while it's exiled", from exile. */
    ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE(Triggers.timeCounterRemovedFromThisInExile(), Zone.EXILE),
    /** "When this creature becomes renowned". */
    ON_SELF_BECOMES_RENOWNED(Triggers.becomesRenowned(), Zone.BATTLEFIELD),
    /** "Whenever a creature you control becomes renowned". */
    ON_ALLY_CREATURE_BECOMES_RENOWNED(Triggers.creatureYouControlBecomesRenowned(), Zone.BATTLEFIELD),
    /** "When this creature becomes monstrous". */
    ON_SELF_BECOMES_MONSTROUS(Triggers.becomesMonstrous(), Zone.BATTLEFIELD),
    /** "Whenever you get one or more {E}". */
    ON_CONTROLLER_GETS_ENERGY(Triggers.youGetEnergy(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more counters on a creature you don't control". */
    ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL(
            Triggers.youPutCountersOnCreatureYouDontControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more counters on a creature". */
    ON_YOU_PUT_COUNTERS_ON_CREATURE(Triggers.youPutCountersOnCreature(), Zone.BATTLEFIELD),
    /**
     * "Whenever one or more +1/+1 counters are put on this permanent"; a {@code TriggerMode.PER_COUNTER} registration
     * triggers once for each counter.
     */
    ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT(Triggers.plusOneCountersPutOnThis(), Zone.BATTLEFIELD),
    /** "Whenever this creature trains". */
    ON_SELF_TRAINS(Triggers.trains(), Zone.BATTLEFIELD),
    /** "Whenever one or more +1/+1 counters are put on a Human you control". */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_HUMAN(Triggers.plusOneCountersPutOnHumanYouControl(), Zone.BATTLEFIELD),
    /** "When this Class becomes level 2". */
    ON_SELF_REACHES_LEVEL_TWO(Triggers.reachesLevelTwo(), Zone.BATTLEFIELD),
    /** "When this Class becomes level 3". */
    ON_SELF_REACHES_LEVEL_THREE(Triggers.reachesLevelThree(), Zone.BATTLEFIELD),
    /** "Whenever one or more counters are put on this permanent". */
    ON_SELF_COUNTERS_PUT(Triggers.countersPutOnThis(), Zone.BATTLEFIELD),
    /** "Whenever one or more +1/+1 counters are put on another non-Hydra creature you control". */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE(
            Triggers.plusOneCountersPutOnAnotherNonHydraCreatureYouControl(), Zone.BATTLEFIELD),
    /** "Whenever one or more +1/+1 counters are put on another creature you control". */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_CREATURE(
            Triggers.plusOneCountersPutOnAnotherCreatureYouControl(), Zone.BATTLEFIELD),
    /**
     * "Whenever you put one or more +1/+1 counters on another permanent you control, if it's the first time +1/+1
     * counters have been put on that permanent this turn".
     */
    ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN(
            Triggers.plusOneCountersPutOnAnotherPermanentYouControlFirstTimeEachTurn(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more +1/+1 counters on a permanent you control". */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT(
            Triggers.youPutPlusOneCountersOnPermanentYouControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more +1/+1 counters on a creature you control". */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE(
            Triggers.youPutPlusOneCountersOnCreatureYouControl(), Zone.BATTLEFIELD),
    /** "Whenever one or more +1/+1 counters are put on a creature you control". */
    ON_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ALLY_CREATURE(
            Triggers.plusOneCountersPutOnCreatureYouControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more +1/+1 counters on a creature". */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE(Triggers.youPutPlusOneCountersOnCreature(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more +1/+1 counters on other Heroes you control". */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_OTHER_HERO(
            Triggers.youPutPlusOneCountersOnAnotherHeroYouControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more +1/+1 counters on another creature". */
    ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANOTHER_CREATURE(
            Triggers.youPutPlusOneCountersOnAnotherCreature(), Zone.BATTLEFIELD),
    /**
     * "Whenever you put one or more +1/+1 counters on a creature you control", for an ability that triggers or does
     * this only once each turn.
     */
    ON_CONTROLLER_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE(
            Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more counters on a permanent or player". */
    ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER(Triggers.youPutCountersOnPermanentOrPlayer(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more time counters on a permanent you control". */
    ON_YOU_PUT_TIME_COUNTERS_ON_CONTROLLED_PERMANENT(
            Triggers.youPutTimeCountersOnPermanentYouControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more counters on another creature". */
    ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE(Triggers.youPutCountersOnAnotherCreature(), Zone.BATTLEFIELD),
    /** "Whenever an opponent puts one or more counters on a creature they control". */
    ON_OPPONENT_PUT_COUNTERS_ON_CREATURE_THEY_CONTROL(
            Triggers.opponentPutsCountersOnCreatureTheyControl(), Zone.BATTLEFIELD),
    /** "Whenever you put a lore counter on a Saga you control", once for each counter. */
    ON_YOU_PUT_LORE_COUNTERS_ON_SAGA(Triggers.youPutLoreCounterOnSagaYouControl(), Zone.BATTLEFIELD),
    /** "Whenever one or more counters are put on a creature you control". */
    ON_ALLY_COUNTER_PUT_ON_CREATURE(Triggers.countersPutOnCreatureYouControl(), Zone.BATTLEFIELD),
    /** "Whenever this creature evolves". */
    ON_SELF_EVOLVES(Triggers.evolves(), Zone.BATTLEFIELD),
    /** "Whenever one or more loyalty counters are removed from this planeswalker". */
    ON_SELF_LOYALTY_COUNTERS_REMOVED(Triggers.loyaltyCountersRemovedFromThis(), Zone.BATTLEFIELD),
    /** "Whenever one or more time counters are removed from this permanent". */
    ON_SELF_TIME_COUNTERS_REMOVED(Triggers.timeCountersRemovedFromThis(), Zone.BATTLEFIELD),
    /** "Whenever a +1/+1 counter is removed from this creature", once for each counter. */
    ON_SELF_PLUS_ONE_PLUS_ONE_COUNTER_REMOVED(Triggers.plusOneCounterRemovedFromThis(), Zone.BATTLEFIELD),
    /** "Whenever one or more counters are removed from a permanent you control". */
    ON_ALLY_COUNTERS_REMOVED_FROM_PERMANENT(Triggers.countersRemovedFromPermanentYouControl(), Zone.BATTLEFIELD),
    /** "Whenever one or more loyalty counters are put on planeswalkers you control". */
    ON_YOU_PUT_LOYALTY_COUNTERS_ON_PLANESWALKERS(
            Triggers.loyaltyCountersPutOnPlaneswalkersYouControl(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more -1/-1 counters on this creature". */
    ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT(Triggers.youPutMinusOneCountersOnThis(), Zone.BATTLEFIELD),
    /** "Whenever a -1/-1 counter is put on a creature", once for each counter. */
    ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE(Triggers.minusOneCounterPutOnCreature(), Zone.BATTLEFIELD),
    /** "Whenever one or more -1/-1 counters are put on a creature". */
    ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE(Triggers.minusOneCountersPutOnCreature(), Zone.BATTLEFIELD),
    /** "Whenever you put a -1/-1 counter on a creature", once for each counter. */
    ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE(Triggers.youPutMinusOneCounterOnCreature(), Zone.BATTLEFIELD),
    /** "Whenever you put one or more -1/-1 counters on a creature". */
    ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE(Triggers.youPutMinusOneCountersOnCreature(), Zone.BATTLEFIELD),
    /** "Whenever you proliferate". */
    ON_CONTROLLER_PROLIFERATES(Triggers.youProliferate(), Zone.BATTLEFIELD),
    /** "Whenever you proliferate", from the graveyard. */
    GRAVEYARD_ON_CONTROLLER_PROLIFERATES(Triggers.youProliferate(), Zone.GRAVEYARD),
    /** "Whenever one or more +1/+1 counters are put on a creature you control", from the graveyard. */
    GRAVEYARD_ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_CREATURE(
            Triggers.plusOneCountersPutOnCreatureYouControl(), Zone.GRAVEYARD);

    private final Trigger trigger;
    private final Zone zone;

    CounterWatch(Trigger trigger, Zone zone) {
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
