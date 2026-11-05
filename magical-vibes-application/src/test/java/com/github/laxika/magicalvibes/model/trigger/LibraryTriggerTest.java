package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.trigger.LibraryAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.LibraryWatch;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/** The draw, discard, cycle, scry, surveil, library search and shuffle triggers: {@link LibraryTrigger}. */
class LibraryTriggerTest {

    private static final CardEffect DRAW = new DrawCardEffect(1);
    private static final CardEffect GAIN = new GainLifeEffect(2);
    private static final CardPredicate ARTIFACT = new CardTypePredicate(CardType.ARTIFACT);
    private static final CardPredicate NONLAND = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

    @Test
    void aPlayerActionFunctionsFromTheBattlefield() {
        assertThat(Triggers.youDrawCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentDiscardsCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.playerCyclesCard().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.youSurveil().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(Triggers.opponentShufflesLibrary().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
    }

    @Test
    void thisCardCycledOrDiscardedFunctionsFromWhereTheCardWindsUp() {
        AbilityZones windsUp = AbilityZones.of(Zone.GRAVEYARD, Zone.EXILE, Zone.LIBRARY);

        assertThat(Triggers.youCycleThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(Triggers.youDiscardThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(Triggers.opponentCausesYouToDiscardThisCard().defaultZones()).isEqualTo(windsUp);
        assertThat(windsUp.functionsIn(Zone.HAND)).isFalse();
        assertThat(windsUp.functionsIn(Zone.BATTLEFIELD)).isFalse();
    }

    @Test
    void namesWhatThePlayerDoesAndWhoDoesIt() {
        assertThat(Triggers.youDrawCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.opponentDrawsCard())
                .isNotEqualTo(Triggers.enchantedPlayerDrawsCard())
                .isNotEqualTo(Triggers.youDrawSecondCardEachTurn());
        assertThat(Triggers.youDrawSecondCardEachTurn()).isNotEqualTo(Triggers.opponentDrawsSecondCardEachTurn());
        assertThat(Triggers.youDiscardCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.DISCARDS, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.opponentDiscardsCard())
                .isNotEqualTo(Triggers.youDiscardOneOrMoreCards());
        assertThat(Triggers.youDiscardThisCard())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.THIS_DISCARDED, PlayerRelation.ANY))
                .isNotEqualTo(Triggers.opponentCausesYouToDiscardThisCard());
        assertThat(Triggers.youCycleThisCard()).isNotEqualTo(Triggers.playerCyclesCard());
        assertThat(Triggers.youScry()).isNotEqualTo(Triggers.opponentScries()).isNotEqualTo(Triggers.youSurveil());
        assertThat(Triggers.youSurveil()).isNotEqualTo(Triggers.opponentSurveils());
        assertThat(Triggers.youSearchYourLibrary()).isNotEqualTo(Triggers.opponentSearchesTheirLibrary());
        assertThat(Triggers.playerShufflesLibrary())
                .isNotEqualTo(Triggers.opponentShufflesLibrary())
                .isNotEqualTo(Triggers.spellOrAbilityCausesItsControllerToShuffle());
    }

    @Test
    void aFilterAndACountAreDroppedFromTheWatchKey() {
        LibraryTrigger artifact = Triggers.youDiscardCard().withFilter(ARTIFACT);
        LibraryTrigger third = Triggers.youDrawNthCardEachTurn(3);

        assertThat(artifact.qualifier()).isEqualTo(ARTIFACT);
        assertThat(artifact.watchKey()).isEqualTo(Triggers.youDiscardCard());
        assertThat(third.nth()).isEqualTo(3);
        assertThat(third.watchKey()).isEqualTo(Triggers.youDrawCard());
        assertThat(Triggers.opponentDrawsSecondCardEachTurn().watchKey()).isEqualTo(Triggers.opponentDrawsCard());
        assertThat(Triggers.youSurveil().firstTimeEachTurn().watchKey()).isEqualTo(Triggers.youSurveil());
        assertThat(Triggers.youScry().watchKey()).isEqualTo(Triggers.youScry());
        assertThatThrownBy(() -> artifact.withFilter(ARTIFACT)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> third.firstTimeEachTurn()).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> Triggers.youDrawNthCardEachTurn(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Triggers.youDiscardCard().withFilter(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void theEventsWithoutACollectorOfTheirOwnAreWatchedByTheCollectorThatReadsThem() {
        assertThat(Triggers.youCycleCard().watchKey()).isEqualTo(Triggers.youDiscardCard());
        assertThat(Triggers.opponentCausesYouToDiscardCard().watchKey()).isEqualTo(Triggers.youDiscardCard());
        assertThat(Triggers.opponentDrawsCardExceptFirstInDrawStep().watchKey())
                .isEqualTo(Triggers.opponentDrawsCard());
        assertThat(Triggers.youDrawFirstCardInDrawStep().watchKey()).isEqualTo(Triggers.youDrawCard());
        assertThat(Triggers.playerCyclesCard().watchKey()).isEqualTo(Triggers.playerCyclesCard());
        assertThat(Triggers.opponentCausesYouToDiscardThisCard().watchKey())
                .isEqualTo(Triggers.opponentCausesYouToDiscardThisCard());
    }

    @Test
    void aLibraryAbilityHasNoLegacyView() {
        Card card = new Card();
        card.addTrigger(Triggers.youDiscardCard().withFilter(ARTIFACT), DRAW);
        TriggeredAbility ability = card.getTriggeredAbilities().getFirst();
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(new TriggeredAbility(Triggers.youDrawCard(), DRAW));

        assertThat(Triggers.youScry().collectedAsAbility()).isTrue();
        assertThat(ability.collectedAsAbility()).isTrue();
        assertThatThrownBy(ability::legacyEffects).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> card.getLegacyTriggerEffects(Triggers.youDiscardCard(), Zone.BATTLEFIELD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> permanent.getLegacyTemporaryTriggerEffects(Triggers.youDrawCard()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_DISCARDS.registrationsOf(card))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void theReaderGivesTheFilterTheLimitAndTheCountAndWrapsTheInterveningIfAroundTheEffect() {
        Card urza = new Card();
        urza.addTrigger(Triggers.youDiscardCard().withFilter(ARTIFACT),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = urza.getTriggeredAbilities().getFirst();
        Card zurzoth = new Card();
        zurzoth.addTrigger(Triggers.opponentDrawsNthCardEachTurn(1),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerIsMonarch()), GAIN);

        assertThat(LibraryAbilityReader.readPrinted(urza, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.ability()).isSameAs(ability);
                    assertThat(read.filter()).isEqualTo(ARTIFACT);
                    assertThat(read.event()).isEqualTo(LibraryTrigger.LibraryEvent.DISCARDS);
                    assertThat(read.counts()).isEmpty();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.ONCE_EACH_TURN);
                });
        assertThat(LibraryAbilityReader.readPrinted(zurzoth, LibraryWatch.ON_OPPONENT_DRAWS))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(new ConditionalEffect(new ControllerIsMonarch(), GAIN, true));
                    assertThat(read.filter()).isNull();
                    assertThat(read.counts()).containsExactly(1);
                    assertThat(read.firstTimeEachTurn()).isTrue();
                    assertThat(read.countMatches(1)).isTrue();
                    assertThat(read.countMatches(2)).isFalse();
                    assertThat(read.limit()).isEqualTo(TriggerLimit.NONE);
                });
        assertThat(urza.getTriggerEffects(Triggers.youDiscardCard(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void theEventConditionIsInsideTheInterveningIf() {
        Card card = new Card();
        card.addTrigger(Triggers.opponentDrawsCard(), TriggerOptions.DEFAULT
                .withInterveningIf(new ControllerIsMonarch()).withEventCondition(new ControllerTurn()), DRAW);

        assertThat(LibraryAbilityReader.printedEffects(card, LibraryWatch.ON_OPPONENT_DRAWS))
                .containsExactly(new ConditionalEffect(new ControllerIsMonarch(),
                        ConditionalEffect.atTriggerTime(new ControllerTurn(), DRAW), true));
    }

    @Test
    void theLimitsAreMarkedByTheAbilityOnItsSource() {
        Card card = new Card();
        card.addTrigger(Triggers.youDrawNthCardEachTurn(3), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE), DRAW);
        card.addTrigger(Triggers.youDrawCard(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), GAIN);
        List<LibraryAbilityReader.LibraryEffect> reads =
                LibraryAbilityReader.readPrinted(card, LibraryWatch.ON_CONTROLLER_DRAWS);
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        reads.getFirst().markLimit(gameData, sourceId);

        assertThat(reads.getFirst().limitReached(gameData, sourceId)).isTrue();
        assertThat(reads.getFirst().limitReached(gameData, UUID.randomUUID())).isFalse();
        assertThat(reads.get(1).limitReached(gameData, sourceId)).isFalse();
        assertThat(gameData.onceOnlyTriggersFired).doesNotContain(sourceId);

        reads.get(1).markLimit(gameData, sourceId);

        assertThat(reads.get(1).limitReached(gameData, sourceId)).isTrue();
        assertThat(gameData.oncePerTurnTriggersFiredThisTurn).doesNotContain(sourceId);
    }

    @Test
    void doThisOnlyOnceEachTurnIsMarkedAsTheActionIsTaken() {
        Card planetarium = new Card();
        planetarium.addTrigger(List.of(Triggers.youScry(), Triggers.youSurveil()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN), DRAW);
        LibraryAbilityReader.LibraryEffect scry =
                LibraryAbilityReader.readPrinted(planetarium, LibraryWatch.ON_CONTROLLER_SCRIES).getFirst();
        LibraryAbilityReader.LibraryEffect surveil =
                LibraryAbilityReader.readPrinted(planetarium, LibraryWatch.ON_CONTROLLER_SURVEILS).getFirst();
        GameData gameData = new GameData(UUID.randomUUID(), "test", UUID.randomUUID(), "owner");
        UUID sourceId = UUID.randomUUID();

        scry.markLimit(gameData, sourceId);

        assertThat(scry.marksLimitOnAcceptance()).isTrue();
        assertThat(scry.limitReached(gameData, sourceId)).isFalse();

        gameData.oncePerTurnTriggersFiredThisTurn.add(sourceId);

        assertThat(scry.limitReached(gameData, sourceId)).isTrue();
        assertThat(surveil.limitReached(gameData, sourceId)).isTrue();
    }

    @Test
    void theFirstTimeEachTurnIsACountOfOne() {
        Card rescuer = new Card();
        rescuer.addTrigger(Triggers.youCycleCard().firstTimeEachTurn(), DRAW);
        Card veronica = new Card();
        veronica.addTrigger(Triggers.youDiscardCard().withFilter(NONLAND).firstTimeEachTurn(), DRAW);
        Card rielle = new Card();
        rielle.addTrigger(Triggers.youDiscardOneOrMoreCards().firstTimeEachTurn(), DRAW);
        Card snitch = new Card();
        snitch.addTrigger(Triggers.youSurveil().firstTimeEachTurn(), DRAW);

        assertThat(LibraryAbilityReader.readPrinted(rescuer, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .extracting(LibraryAbilityReader.LibraryEffect::event, LibraryAbilityReader.LibraryEffect::filter,
                        LibraryAbilityReader.LibraryEffect::firstTimeEachTurn)
                .containsExactly(tuple(LibraryTrigger.LibraryEvent.CYCLES, null, true));
        assertThat(LibraryAbilityReader.readPrinted(veronica, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .extracting(LibraryAbilityReader.LibraryEffect::event, LibraryAbilityReader.LibraryEffect::filter,
                        LibraryAbilityReader.LibraryEffect::firstTimeEachTurn)
                .containsExactly(tuple(LibraryTrigger.LibraryEvent.DISCARDS, NONLAND, true));
        assertThat(LibraryAbilityReader.readPrinted(rielle, LibraryWatch.ON_CONTROLLER_DISCARD_EVENT))
                .extracting(LibraryAbilityReader.LibraryEffect::firstTimeEachTurn).containsExactly(true);
        assertThat(LibraryAbilityReader.readPrinted(snitch, LibraryWatch.ON_CONTROLLER_SURVEILS))
                .extracting(LibraryAbilityReader.LibraryEffect::firstTimeEachTurn).containsExactly(true);
    }

    @Test
    void theNarrowedEventsAreReadByTheCollectorThatWatchesThem() {
        Card cycle = new Card();
        cycle.addTrigger(Triggers.youCycleCard(), DRAW);
        Card focus = new Card();
        focus.addTrigger(Triggers.opponentCausesYouToDiscardCard(), DRAW);
        Card bowmasters = new Card();
        bowmasters.addTrigger(Triggers.opponentDrawsCardExceptFirstInDrawStep(), DRAW);
        Card wiretapping = new Card();
        wiretapping.addTrigger(Triggers.youDrawFirstCardInDrawStep(), DRAW);
        Card octopus = new Card();
        octopus.addTrigger(List.of(Triggers.youDrawNthCardEachTurn(1), Triggers.youDrawSecondCardEachTurn()), DRAW);

        assertThat(LibraryAbilityReader.readPrinted(cycle, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .extracting(LibraryAbilityReader.LibraryEffect::event)
                .containsExactly(LibraryTrigger.LibraryEvent.CYCLES);
        assertThat(LibraryAbilityReader.readPrinted(focus, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .extracting(LibraryAbilityReader.LibraryEffect::event)
                .containsExactly(LibraryTrigger.LibraryEvent.DISCARDS_BY_OPPONENT);
        assertThat(LibraryAbilityReader.readPrinted(bowmasters, LibraryWatch.ON_OPPONENT_DRAWS))
                .extracting(LibraryAbilityReader.LibraryEffect::event)
                .containsExactly(LibraryTrigger.LibraryEvent.DRAWS_EXCEPT_FIRST_IN_DRAW_STEP);
        assertThat(LibraryAbilityReader.readPrinted(wiretapping, LibraryWatch.ON_CONTROLLER_DRAWS))
                .extracting(LibraryAbilityReader.LibraryEffect::event)
                .containsExactly(LibraryTrigger.LibraryEvent.DRAWS_FIRST_IN_DRAW_STEP);
        assertThat(LibraryAbilityReader.readPrinted(octopus, LibraryWatch.ON_CONTROLLER_DRAWS))
                .singleElement().satisfies(read -> {
                    assertThat(read.counts()).containsExactlyInAnyOrder(1, 2);
                    assertThat(read.countMatches(1)).isTrue();
                    assertThat(read.countMatches(2)).isTrue();
                    assertThat(read.countMatches(3)).isFalse();
                    assertThat(read.firstTimeEachTurn()).isFalse();
                });
        assertThat(LibraryAbilityReader.printed(cycle, LibraryWatch.ON_ANY_PLAYER_CYCLES)).isEmpty();
    }

    @Test
    void theStrictReaderRejectsAFilterANarrowerEventACountOrALimit() {
        Card filtered = new Card();
        filtered.addTrigger(Triggers.youDiscardCard().withFilter(ARTIFACT), DRAW);
        Card narrower = new Card();
        narrower.addTrigger(Triggers.youCycleCard(), DRAW);
        Card counted = new Card();
        counted.addTrigger(Triggers.youDrawSecondCardEachTurn(), DRAW);
        Card limited = new Card();
        limited.addTrigger(Triggers.youScry(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        Card plain = new Card();
        plain.addTrigger(Triggers.youCycleThisCard(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerIsMonarch()), DRAW);

        assertThatThrownBy(() -> LibraryAbilityReader.printedEffects(filtered, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.printedEffects(narrower, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.printedEffects(counted, LibraryWatch.ON_CONTROLLER_DRAWS))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.printedEffects(limited, LibraryWatch.ON_CONTROLLER_SCRIES))
                .isInstanceOf(IllegalStateException.class);
        assertThat(LibraryAbilityReader.printedEffects(plain, LibraryWatch.ON_SELF_CYCLED))
                .containsExactly(new ConditionalEffect(new ControllerIsMonarch(), DRAW, true));
    }

    @Test
    void aMultiplicityALimitOrTwoReadingsTheLibraryCollectorsCantReadAreRejected() {
        Card perCreature = new Card();
        perCreature.addTrigger(Triggers.youDrawCard(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE), DRAW);
        Card twoReadings = new Card();
        twoReadings.addTrigger(List.of(Triggers.youDiscardCard().withFilter(ARTIFACT), Triggers.youDiscardCard()),
                DRAW);
        Card countedAndNot = new Card();
        countedAndNot.addTrigger(List.of(Triggers.youDrawSecondCardEachTurn(), Triggers.youDrawCard()), DRAW);

        assertThatThrownBy(() -> new Card().addTrigger(Triggers.youCycleCard(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.readPrinted(perCreature, LibraryWatch.ON_CONTROLLER_DRAWS))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.readPrinted(twoReadings, LibraryWatch.ON_CONTROLLER_DISCARDS))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryAbilityReader.readPrinted(countedAndNot, LibraryWatch.ON_CONTROLLER_DRAWS))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void anAbilityOfTwoWatchesIsReadByEachForItsOwnCondition() {
        Card hostile = new Card();
        hostile.addTrigger(List.of(Triggers.youDiscardOneOrMoreCards(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = hostile.getTriggeredAbilities().getFirst();

        assertThat(LibraryAbilityReader.readPrinted(hostile, LibraryWatch.ON_CONTROLLER_DISCARD_EVENT))
                .extracting(LibraryAbilityReader.LibraryEffect::event, LibraryAbilityReader.LibraryEffect::ability)
                .containsExactly(tuple(LibraryTrigger.LibraryEvent.DISCARDS_ONE_OR_MORE, ability));
        assertThat(LibraryAbilityReader.readPrinted(hostile, LibraryWatch.ON_OPPONENT_DISCARDS))
                .extracting(LibraryAbilityReader.LibraryEffect::event, LibraryAbilityReader.LibraryEffect::ability)
                .containsExactly(tuple(LibraryTrigger.LibraryEvent.DISCARDS, ability));
    }

    @Test
    void grantedAbilitiesAreFoundByTheWatchedTriggerAndReadWithTheirCount() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.youDrawNthCardEachTurn(3), DRAW);
        Permanent permanent = new Permanent(new Card());
        permanent.addTemporaryAbility(granted);

        assertThat(granted.hasWatchedTrigger(LibraryWatch.ON_CONTROLLER_DRAWS.trigger())).isTrue();
        assertThat(LibraryAbilityReader.readGrantedToPermanent(permanent, LibraryWatch.ON_CONTROLLER_DRAWS.trigger()))
                .singleElement().satisfies(read -> {
                    assertThat(read.effect()).isEqualTo(DRAW);
                    assertThat(read.counts()).containsExactly(3);
                });
        assertThat(LibraryAbilityReader.readGrantedToPermanent(permanent, LibraryWatch.ON_OPPONENT_DRAWS.trigger()))
                .isEmpty();
    }

    @Test
    void needsAnEventAndAPlayer() {
        assertThatThrownBy(() -> new LibraryTrigger(null, PlayerRelation.YOU))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LibraryTrigger(LibraryTrigger.LibraryEvent.DRAWS, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aCardReadsItsLibraryAbilitiesByTriggerAndZone() {
        Card card = new Card();
        card.addTrigger(Triggers.youDrawSecondCardEachTurn(), DRAW);
        card.addTrigger(new TriggerCondition(Triggers.youDrawSecondCardEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, GAIN);

        assertThat(card.getTriggerEffects(Triggers.youDrawSecondCardEachTurn(), Zone.BATTLEFIELD))
                .containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youDrawSecondCardEachTurn(), Zone.GRAVEYARD))
                .containsExactly(GAIN);
        assertThat(card.getTriggerEffects(Triggers.youDrawCard(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aCycledCardReadsItsCyclingAbilityFromTheGraveyard() {
        Card card = new Card();
        card.addTrigger(Triggers.youCycleThisCard(), DRAW);

        assertThat(card.getTriggerEffects(Triggers.youCycleThisCard(), Zone.GRAVEYARD)).containsExactly(DRAW);
        assertThat(card.getTriggerEffects(Triggers.youCycleThisCard(), Zone.BATTLEFIELD)).isEmpty();
    }
}
