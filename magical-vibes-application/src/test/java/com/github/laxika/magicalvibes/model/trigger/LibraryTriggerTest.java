package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
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
    void aCardRevealedThisWayIsAnEventOfItsOwnWithTheRevealedCardsFilter() {
        LibraryTrigger nonland = Triggers.youRevealCardThisWay().withFilter(NONLAND);

        assertThat(Triggers.youRevealCardThisWay())
                .isEqualTo(new LibraryTrigger(LibraryTrigger.LibraryEvent.REVEALS_CARD_THIS_WAY, PlayerRelation.YOU))
                .isNotEqualTo(Triggers.youDrawCard());
        assertThat(Triggers.youRevealCardThisWay().defaultZones()).isEqualTo(AbilityZones.BATTLEFIELD);
        assertThat(nonland.qualifier()).isEqualTo(NONLAND);
        assertThat(nonland.watchKey()).isEqualTo(Triggers.youRevealCardThisWay());
        assertThat(Triggers.youDrawCard().withFilter(ARTIFACT).watchKey()).isEqualTo(Triggers.youDrawCard());
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

        assertThatThrownBy(ability::ownEntryEffects).isInstanceOf(IllegalStateException.class);
        assertThat(card.getTriggerEffects(Triggers.youDiscardCard(), Zone.BATTLEFIELD)).isEmpty();
    }

    @Test
    void aLibraryTriggerTakesNoMultiplicity() {
        assertThatThrownBy(() -> new Card().addTrigger(Triggers.youCycleCard(), TriggerOptions.ONE_OR_MORE, DRAW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAbilityIsFoundByTheWatchKeyOfEachOfItsConditions() {
        TriggeredAbility granted = new TriggeredAbility(Triggers.youDrawNthCardEachTurn(3), DRAW);
        Card card = new Card();
        card.addTrigger(List.of(Triggers.youDiscardOneOrMoreCards(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), DRAW);
        TriggeredAbility hostile = card.getTriggeredAbilities().getFirst();

        assertThat(granted.hasWatchedTrigger(Triggers.youDrawCard())).isTrue();
        assertThat(granted.hasWatchedTrigger(Triggers.opponentDrawsCard())).isFalse();
        assertThat(hostile.hasWatchedTrigger(Triggers.youDiscardOneOrMoreCards())).isTrue();
        assertThat(hostile.hasWatchedTrigger(Triggers.opponentDiscardsCard())).isTrue();
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
