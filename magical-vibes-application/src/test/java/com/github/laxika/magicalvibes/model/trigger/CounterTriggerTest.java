package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The counter triggers: {@link CounterTrigger}. */
class CounterTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);
    private static final PermanentPredicate COLORLESS = new PermanentIsColorlessPredicate();

    @Test
    void functionsFromTheBattlefieldExceptTheExiledTimeCounterTrigger() {
        assertThat(Triggers.countersPutOnThis().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youProliferate().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.timeCountersRemovedFromThis().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.timeCounterRemovedFromThisInExile().defaultZones()).isEqualTo(AbilityZones.EXILE);
        assertThat(Triggers.youRemoveTimeCounterFromThisInExile().defaultZones()).isEqualTo(AbilityZones.EXILE);
    }

    @Test
    void namesTheEventWhoseObjectItIsAndWhatItMustBe() {
        assertThat(Triggers.plusOneCountersPutOnCreatureYouControl())
                .isEqualTo(new CounterTrigger(CounterTrigger.CounterEvent.PLUS_ONE_COUNTERS_PUT,
                        ObjectRelation.YOU_CONTROL, Triggers.CREATURE))
                .isNotEqualTo(Triggers.plusOneCountersPutOnHumanYouControl())
                .isNotEqualTo(Triggers.plusOneCountersPutOnAnotherCreatureYouControl())
                .isNotEqualTo(Triggers.plusOneCountersPutOnThis())
                .isNotEqualTo(Triggers.youPutPlusOneCountersOnCreatureYouControl());
        assertThat(Triggers.plusOneCountersPutOnAnotherCreatureYouControl())
                .isNotEqualTo(Triggers.plusOneCountersPutOnAnotherNonHydraCreatureYouControl());
        assertThat(Triggers.youPutCountersOnCreature())
                .isNotEqualTo(Triggers.youPutCountersOnAnotherCreature())
                .isNotEqualTo(Triggers.youPutCountersOnCreatureYouDontControl())
                .isNotEqualTo(Triggers.youPutCountersOnPermanentOrPlayer());
        assertThat(Triggers.minusOneCountersPutOnCreature())
                .isNotEqualTo(Triggers.minusOneCounterPutOnCreature())
                .isNotEqualTo(Triggers.youPutMinusOneCountersOnCreature());
        assertThat(Triggers.youPutMinusOneCountersOnThis()).isNotEqualTo(Triggers.youPutMinusOneCountersOnCreature());
        assertThat(Triggers.reachesLevelTwo()).isNotEqualTo(Triggers.reachesLevelThree());
        assertThat(Triggers.becomesRenowned()).isNotEqualTo(Triggers.creatureYouControlBecomesRenowned());
    }

    @Test
    void aFilterIsDroppedFromTheWatchKey() {
        CounterTrigger colorless = Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(COLORLESS);

        assertThat(colorless.qualifier()).isEqualTo(COLORLESS);
        assertThat(colorless.filter()).isEqualTo(Triggers.CREATURE);
        assertThat(colorless.watchKey()).isEqualTo(Triggers.youPutPlusOneCountersOnAnotherCreature());
        assertThat(Triggers.countersPutOnThis().watchKey()).isEqualTo(Triggers.countersPutOnThis());
        assertThatThrownBy(() -> colorless.withFilter(COLORLESS)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.countersPutOnThis().withFilter(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void theEventsWithoutACollectorOfTheirOwnAreWatchedByTheEventAboutAnyPlayer() {
        assertThat(Triggers.youPutCountersOnCreatureYouControl().watchKey())
                .isEqualTo(Triggers.countersPutOnCreatureYouControl());
        assertThat(Triggers.youPutPlusOneCountersOnThis().watchKey()).isEqualTo(Triggers.plusOneCountersPutOnThis());
        assertThat(Triggers.plusOneCounterPutOnThis().watchKey()).isEqualTo(Triggers.plusOneCountersPutOnThis());
        assertThat(Triggers.youRemoveTimeCounterFromThisInExile().watchKey())
                .isEqualTo(Triggers.timeCounterRemovedFromThisInExile());
        assertThat(Triggers.youPutCountersOnCreature().watchKey()).isEqualTo(Triggers.youPutCountersOnCreature());
        assertThat(Triggers.youPutPlusOneCountersOnCreatureYouControl().watchKey())
                .isEqualTo(Triggers.youPutPlusOneCountersOnCreatureYouControl());
    }

    @Test
    void aMultiplicityNoCounterCollectorReadsIsRejectedAsTheAbilityIsBuilt() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.countersPutOnThis(), TriggerOptions.ONE_OR_MORE,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.countersPutOnThis(), TriggerOptions.EACH_SOURCE,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void timeCountersRemovedInExileIsNotTheBattlefieldTrigger() {
        assertThat(Triggers.timeCounterRemovedFromThisInExile()).isNotEqualTo(Triggers.timeCountersRemovedFromThis());
    }

    @Test
    void needsAnEventAndARelation() {
        assertThatThrownBy(() -> new CounterTrigger(null, ObjectRelation.THIS))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CounterTrigger(CounterTrigger.CounterEvent.COUNTERS_PUT, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsCounterAbilitiesByTheZoneTheyFunctionFrom() {
        Card card = new Card();
        card.addTrigger(Triggers.youProliferate(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youProliferate(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, GAIN);

        assertThat(card.getTriggerEffects(Triggers.youProliferate(), Zone.BATTLEFIELD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youProliferate(), Zone.GRAVEYARD)).containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youGetEnergy(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void anExiledTimeCounterAbilityFunctionsFromExile() {
        Card card = new Card();
        card.addTrigger(Triggers.timeCounterRemovedFromThisInExile(), DRAW);

        assertThat(card.getTriggerEffects(Triggers.timeCounterRemovedFromThisInExile(), Zone.EXILE))
                .containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.timeCounterRemovedFromThisInExile(), Zone.BATTLEFIELD))
                .isEmpty();
    }
}
