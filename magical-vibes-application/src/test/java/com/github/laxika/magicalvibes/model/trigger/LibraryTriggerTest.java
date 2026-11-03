package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExceptFirstDrawStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstDrawStepDrawOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.FirstOrSecondCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OnceOnlyTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentCausedDiscardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.trigger.LibraryWatch;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void theLegacyViewGivesTheFilterOutsideTheLimitAndTheIfInsideTheCount() {
        Card urza = new Card();
        urza.addTrigger(Triggers.youDiscardCard().withFilter(ARTIFACT),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility ability = urza.getTriggeredAbilities().getFirst();
        Card zurzoth = new Card();
        zurzoth.addTrigger(Triggers.opponentDrawsNthCardEachTurn(1),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerIsMonarch()), GAIN);

        List<CardEffect> legacy = LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(urza);

        assertThat(legacy).containsExactly(new TriggeringCardConditionalEffect(ARTIFACT,
                OncePerTurnTriggerEffect.keyed(DRAW, ability.limitKey())));
        assertThat(LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(urza).getFirst()).isSameAs(legacy.getFirst());
        assertThat(urza.getTriggerEffects(Triggers.youDiscardCard(), Zone.BATTLEFIELD)).isEmpty();
        assertThat(LibraryWatch.ON_OPPONENT_DRAWS.effectsOf(zurzoth)).containsExactly(
                new NthCardDrawTriggerEffect(1, new ConditionalEffect(new ControllerIsMonarch(), GAIN)));
    }

    @Test
    void theLegacyViewGivesThisAbilityTriggersOnlyOnceOutsideTheCount() {
        Card emerald = new Card();
        emerald.addTrigger(Triggers.youDrawNthCardEachTurn(3), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE),
                DRAW);

        assertThat(LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(emerald))
                .containsExactly(new OnceOnlyTriggerEffect(new NthCardDrawTriggerEffect(3, DRAW)));
    }

    @Test
    void theFirstTimeEachTurnIsTheWrapperThatCountsThePlayersEvents() {
        Card rescuer = new Card();
        rescuer.addTrigger(Triggers.youCycleCard().firstTimeEachTurn(), DRAW);
        Card veronica = new Card();
        veronica.addTrigger(Triggers.youDiscardCard().withFilter(NONLAND).firstTimeEachTurn(), DRAW);
        Card rielle = new Card();
        rielle.addTrigger(Triggers.youDiscardOneOrMoreCards().firstTimeEachTurn(), DRAW);
        Card snitch = new Card();
        snitch.addTrigger(Triggers.youSurveil().firstTimeEachTurn(), DRAW);

        assertThat(LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(rescuer))
                .containsExactly(OncePerTurnTriggerEffect.firstCycling(new CyclingTriggerEffect(DRAW)));
        assertThat(LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(veronica)).containsExactly(
                new TriggeringCardConditionalEffect(NONLAND, OncePerTurnTriggerEffect.firstNonlandDiscard(DRAW)));
        assertThat(LibraryWatch.ON_CONTROLLER_DISCARD_EVENT.effectsOf(rielle))
                .containsExactly(OncePerTurnTriggerEffect.firstDiscardEvent(DRAW));
        assertThat(LibraryWatch.ON_CONTROLLER_SURVEILS.effectsOf(snitch))
                .containsExactly(OncePerTurnTriggerEffect.firstSurveil(DRAW));
    }

    @Test
    void theNarrowedEventsAreTheirCollectorsWrappers() {
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

        assertThat(LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(cycle))
                .containsExactly(new CyclingTriggerEffect(DRAW));
        assertThat(LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(focus))
                .containsExactly(new OpponentCausedDiscardTriggerEffect(DRAW));
        assertThat(LibraryWatch.ON_OPPONENT_DRAWS.effectsOf(bowmasters))
                .containsExactly(new ExceptFirstDrawStepTriggerEffect(DRAW));
        assertThat(LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(wiretapping))
                .containsExactly(new FirstDrawStepDrawOnlyTriggerEffect(DRAW));
        assertThat(LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(octopus))
                .containsExactly(new FirstOrSecondCardDrawTriggerEffect(DRAW));
        assertThat(LibraryWatch.ON_ANY_PLAYER_CYCLES.effectsOf(cycle)).isEmpty();
    }

    @Test
    void aCountFilterOrMultiplicityTheLibraryCollectorsCantReadIsRejected() {
        Card scryCount = new Card();
        scryCount.addTrigger(Triggers.youScry().firstTimeEachTurn(), DRAW);
        Card drawFilter = new Card();
        drawFilter.addTrigger(Triggers.youDrawCard().withFilter(ARTIFACT).nthTimeEachTurn(2), DRAW);
        Card twoCounts = new Card();
        twoCounts.addTrigger(List.of(Triggers.youDrawSecondCardEachTurn(), Triggers.youDrawNthCardEachTurn(3)), DRAW);
        Card oneOrMore = new Card();
        oneOrMore.addTrigger(Triggers.youCycleCard(), TriggerOptions.ONE_OR_MORE, DRAW);
        Card limitAndFirst = new Card();
        limitAndFirst.addTrigger(Triggers.youSurveil().firstTimeEachTurn(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);

        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_SCRIES.effectsOf(scryCount))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(drawFilter))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(twoCounts))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_DISCARDS.effectsOf(oneOrMore))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> LibraryWatch.ON_CONTROLLER_SURVEILS.effectsOf(limitAndFirst))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedAbilitiesAreReadInTheLegacyForm() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.youDrawSecondCardEachTurn(), DRAW);

        assertThat(granted.hasWatchedTrigger(LibraryWatch.ON_CONTROLLER_DRAWS.trigger())).isTrue();
        assertThat(granted.legacyEffects()).extracting(EffectRegistration::effect)
                .containsExactly(new NthCardDrawTriggerEffect(2, DRAW));
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
