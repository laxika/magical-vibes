package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/** The counter triggers: {@link CounterTrigger}. */
class CounterTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(1);

    @Test
    void functionsFromTheBattlefieldExceptTheExiledTimeCounterTrigger() {
        assertThat(Triggers.countersPutOnThis().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youProliferate().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.timeCountersRemovedFromThis().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.timeCounterRemovedFromThisInExile().defaultZones()).isEqualTo(AbilityZones.EXILE);
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
    void keepsTheOnceEachTurnFormOfYouPutPlusOneCountersApart() {
        assertThat(Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn())
                .isNotEqualTo(Triggers.youPutPlusOneCountersOnCreatureYouControl());
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

    @Test
    void aRegistrationKeepsItsTriggerMode() {
        Card card = new Card();
        card.addTrigger(Triggers.plusOneCountersPutOnThis(), TriggerMode.PER_COUNTER, DRAW);
        card.addTrigger(Triggers.plusOneCountersPutOnThis(), GAIN);

        assertThat(card.getTriggerEffectRegistrations(Triggers.plusOneCountersPutOnThis(), Zone.BATTLEFIELD))
                .extracting(EffectRegistration::effect, EffectRegistration::triggerMode)
                .containsExactly(
                        tuple(DRAW, TriggerMode.PER_COUNTER),
                        tuple(GAIN, TriggerMode.NORMAL));
    }
}
