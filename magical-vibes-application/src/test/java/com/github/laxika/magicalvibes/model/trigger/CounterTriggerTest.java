package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.YouPutCounterOnControlledCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.YouRemoveTimeCounterFromExiledCardTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

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
    void theLegacyViewGivesYouPutAndTheFilterOutsideTheLimitAndTheLimitOutsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.youPutCountersOnCreatureYouControl().withFilter(COLORLESS), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        List<CardEffect> legacy = CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE.effectsOf(card);

        assertThat(legacy).containsExactly(new YouPutCounterOnControlledCreatureTriggerEffect(
                new TriggeringPermanentConditionalEffect(COLORLESS, OncePerTurnTriggerEffect.keyed(
                        new ConditionalEffect(new ControllerTurn(), DRAW), ability.limitKey()))));
        assertThat(CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE.effectsOf(card).getFirst()).isSameAs(legacy.getFirst());
        assertThat(card.getTriggerEffects(Triggers.countersPutOnCreatureYouControl(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theLegacyViewGivesTheOtherLimitsAndTheYouRemoveWrapper() {
        Card perCreature = new Card();
        perCreature.addTrigger(Triggers.countersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), DRAW);
        Card doOnce = new Card();
        doOnce.addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), GAIN);
        Card youRemove = new Card();
        youRemove.addTrigger(Triggers.youRemoveTimeCounterFromThisInExile(), DRAW);

        assertThat(CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE.effectsOf(perCreature))
                .containsExactly(new OncePerTurnPerCreatureTriggerEffect(DRAW));
        assertThat(CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE.effectsOf(doOnce))
                .containsExactly(OncePerTurnTriggerEffect.markOnAcceptance(GAIN));
        assertThat(CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE.effectsOf(youRemove))
                .containsExactly(new YouRemoveTimeCounterFromExiledCardTriggerEffect(DRAW));
    }

    @Test
    void eachCounterIsAPerCounterRegistration() {
        Card card = new Card();
        card.addTrigger(Triggers.plusOneCounterPutOnThis(), DRAW);
        card.addTrigger(Triggers.youPutPlusOneCountersOnThis(), GAIN);

        assertThat(CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT.registrationsOf(card))
                .extracting(EffectRegistration::effect, EffectRegistration::triggerMode)
                .containsExactly(
                        tuple(DRAW, TriggerMode.PER_COUNTER),
                        tuple(new YouPutCounterOnControlledCreatureTriggerEffect(GAIN), TriggerMode.NORMAL));
    }

    @Test
    void aMultiplicityOrTwoFiltersTheCounterCollectorsCantReadAreRejected() {
        Card oneOrMore = new Card();
        oneOrMore.addTrigger(Triggers.countersPutOnThis(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card twoReadings = new Card();
        twoReadings.addTrigger(List.of(Triggers.plusOneCounterPutOnThis(), Triggers.plusOneCountersPutOnThis()),
                DRAW);

        assertThatThrownBy(() -> CounterWatch.ON_SELF_COUNTERS_PUT.effectsOf(oneOrMore))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT.effectsOf(twoReadings))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAbilityThatFunctionsFromTwoZonesIsReadByTheWatchOfEach() {
        Card card = new Card();
        card.addTrigger(new TriggerCondition(Triggers.youProliferate(),
                        AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD)),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);

        assertThat(CounterWatch.ON_CONTROLLER_PROLIFERATES.effectsOf(card))
                .containsExactly(new ConditionalEffect(new ControllerTurn(), DRAW));
        assertThat(CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES.effectsOf(card).getFirst())
                .isSameAs(CounterWatch.ON_CONTROLLER_PROLIFERATES.effectsOf(card).getFirst());
    }

    @Test
    void grantedAbilitiesAreReadInTheLegacyForm() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.countersPutOnThis(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);

        assertThat(granted.hasWatchedTrigger(CounterWatch.ON_SELF_COUNTERS_PUT.trigger())).isTrue();
        assertThat(granted.legacyEffects()).extracting(EffectRegistration::effect)
                .containsExactly(OncePerTurnTriggerEffect.keyed(DRAW, granted.limitKey()));
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
