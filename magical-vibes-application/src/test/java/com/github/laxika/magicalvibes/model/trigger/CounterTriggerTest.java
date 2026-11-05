package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsColorlessPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.trigger.CounterAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.CounterWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

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
    void aCounterAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.countersPutOnThis(), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(ability);

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CounterWatch.ON_SELF_COUNTERS_PUT.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> CounterWatch.ON_SELF_COUNTERS_PUT.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theReaderGivesYouPutTheFilterAndTheLimitAndWrapsTheInterveningIfAroundTheEffect() {
        Card card = new Card();
        card.addTrigger(Triggers.youPutCountersOnCreatureYouControl().withFilter(COLORLESS), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();

        List<CounterAbilityReader.CounterEffect> reads =
                CounterAbilityReader.readPrinted(card, CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE);

        assertThat(reads).singleElement().satisfies(read -> {
            assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerTurn(), DRAW, true));
            assertThat(read.ability()).isSameAs(ability);
            assertThat(read.filter()).isEqualTo(COLORLESS);
            assertThat(read.byController()).isTrue();
            assertThat(read.eachCounter()).isFalse();
            assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
        });
        assertThat(card.getTriggerEffects(Triggers.countersPutOnCreatureYouControl(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theEventConditionIsInsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerTurn()).withEventCondition(new SourceCounterThreshold(4,
                        CounterType.PLAN)), DRAW);

        assertThat(CounterAbilityReader.readPrinted(card, CounterWatch.ON_SELF_COUNTERS_PUT))
                .extracting(CounterAbilityReader.CounterEffect::effect)
                .containsExactly(new ConditionalEffect(new ControllerTurn(), ConditionalEffect.atTriggerTime(
                        new SourceCounterThreshold(4, CounterType.PLAN), DRAW), true));
    }

    @Test
    void triggersOnlyOnceEachTurnIsMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.plusOneCountersPutOnThis(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        card.addTrigger(Triggers.plusOneCountersPutOnThis(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        List<CounterAbilityReader.CounterEffect> reads =
                CounterAbilityReader.readPrinted(card, CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(reads.getFirst().limitReached(gameData, sourceId)).isTrue();
        assertThat(reads.getFirst().limitReached(gameData, UUID.randomUUID())).isFalse();
        assertThat(reads.get(1).limitReached(gameData, sourceId)).isFalse();
        assertThat(gameData.oncePerTurnTriggersFiredThisTurn).doesNotContain(sourceId);
    }

    @Test
    void theOtherLimitsEachCounterAndYouRemoveAreReadFromTheAbility() {
        Card perCreature = new Card();
        perCreature.addTrigger(Triggers.countersPutOnCreatureYouControl(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), DRAW);
        Card doOnce = new Card();
        doOnce.addTrigger(Triggers.youPutPlusOneCountersOnCreature(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), GAIN);
        Card youRemove = new Card();
        youRemove.addTrigger(Triggers.youRemoveTimeCounterFromThisInExile(), DRAW);
        Card eachCounter = new Card();
        eachCounter.addTrigger(Triggers.plusOneCounterPutOnThis(), DRAW);
        eachCounter.addTrigger(Triggers.youPutPlusOneCountersOnThis(), GAIN);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();
        UUID creatureId = UUID.randomUUID();

        CounterAbilityReader.CounterEffect forEachCreature = CounterAbilityReader.readPrinted(perCreature,
                CounterWatch.ON_ALLY_COUNTER_PUT_ON_CREATURE).getFirst();
        forEachCreature.markLimit(gameData, sourceId, creatureId);

        assertThat(forEachCreature.limitReached(gameData, sourceId, creatureId)).isTrue();
        assertThat(forEachCreature.limitReached(gameData, sourceId, UUID.randomUUID())).isFalse();
        assertThat(CounterAbilityReader.readPrinted(doOnce,
                CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE).getFirst().marksLimitOnAcceptance())
                .isTrue();
        assertThat(CounterAbilityReader.readPrinted(youRemove, CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.byController()).isTrue();
                });
        assertThat(CounterAbilityReader.readPrinted(eachCounter, CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT))
                .extracting(CounterAbilityReader.CounterEffect::effect, CounterAbilityReader.CounterEffect::eachCounter,
                        CounterAbilityReader.CounterEffect::byController)
                .containsExactly(tuple(DRAW, true, false), tuple(GAIN, false, true));
    }

    @Test
    void theStrictReaderRejectsAFilterANarrowerEventOrALimit() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature().withFilter(COLORLESS), DRAW);
        Card narrower = new Card();
        narrower.addTrigger(Triggers.youPutPlusOneCountersOnThis(), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.youGetEnergy(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.youGetEnergy(), TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                DRAW);

        assertThatThrownBy(() -> CounterAbilityReader.printedEffects(filtered,
                CounterWatch.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANOTHER_CREATURE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CounterAbilityReader.printedEffects(narrower,
                CounterWatch.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> CounterAbilityReader.printedEffects(limited, CounterWatch.ON_CONTROLLER_GETS_ENERGY))
                .isInstanceOf(IllegalStateException.class);
        assertThat(CounterAbilityReader.printedEffects(plain, CounterWatch.ON_CONTROLLER_GETS_ENERGY))
                .containsExactly(new ConditionalEffect(new ControllerTurn(), DRAW, true));
    }

    @Test
    void aMultiplicityNoCounterCollectorReadsIsRejectedAsTheAbilityIsBuilt() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.countersPutOnThis(), TriggerOptions.ONE_OR_MORE,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.countersPutOnThis(), TriggerOptions.EACH_SOURCE,
                DRAW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAbilityThatFunctionsFromTwoZonesIsReadByTheWatchOfEach() {
        Card card = new Card();
        card.addTrigger(new TriggerCondition(Triggers.youProliferate(),
                        AbilityZones.of(Zone.BATTLEFIELD, Zone.GRAVEYARD)),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()), DRAW);

        assertThat(CounterAbilityReader.printedEffects(card, CounterWatch.ON_CONTROLLER_PROLIFERATES))
                .containsExactly(new ConditionalEffect(new ControllerTurn(), DRAW));
        assertThat(CounterAbilityReader.printed(card, CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES))
                .containsExactlyElementsOf(CounterAbilityReader.printed(card, CounterWatch.ON_CONTROLLER_PROLIFERATES));
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadWithTheirLimit() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.countersPutOnThis(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);

        assertThat(granted.hasWatchedTrigger(CounterWatch.ON_SELF_COUNTERS_PUT.trigger())).isTrue();
        assertThat(CounterAbilityReader.read(granted, CounterWatch.ON_SELF_COUNTERS_PUT.trigger()))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
                });
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
