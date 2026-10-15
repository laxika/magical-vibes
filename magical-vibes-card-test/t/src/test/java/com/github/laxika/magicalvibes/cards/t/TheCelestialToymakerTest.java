package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCelestialToymaker.class, Forest.class, Island.class, Swamp.class})
class TheCelestialToymakerTest extends BaseCardTest {

    @Test
    void defendingPlayerChoosesBetweenFaceUpAndFaceDownPiles() {
        addReadyToymaker();
        Card faceUpOne = new Forest();
        Card faceUpTwo = new Island();
        Card faceDown = new Swamp();
        harness.setLibrary(player1, List.of(faceUpOne, faceUpTwo, faceDown));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(faceUpOne.getId(), faceUpTwo.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(faceUpOne, faceUpTwo);
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(faceDown);
            assertThat(exiled.faceDown()).isTrue();
        });
        assertThat(gd.pileGroupingOrGuessCountThisTurn).isEqualTo(1);
    }

    @Test
    void endStepLifeLossUsesDistinctPileGroupingCount() {
        addReadyToymaker();
        Card first = new Forest();
        Card second = new Island();
        Card third = new Swamp();
        harness.setLibrary(player1, List.of(first, second, third));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player2, true);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void bothPilesAreExiledBeforeDefendingPlayerChooses() {
        addReadyToymaker();
        Card faceUp = new Forest();
        Card faceDown = new Swamp();
        harness.setLibrary(player1, List.of(faceUp, faceDown));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(faceUp.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(faceUp);
            assertThat(exiled.faceDown()).isFalse();
        });
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(faceDown);
            assertThat(exiled.faceDown()).isTrue();
        });
    }

    @Test
    void defendingPlayerCanChooseFaceDownPile() {
        addReadyToymaker();
        Card faceUp = new Forest();
        Card faceDown = new Swamp();
        harness.setLibrary(player1, List.of(faceUp, faceDown));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(faceUp.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(faceDown).doesNotContain(faceUp);
        assertThat(gd.exiledCards).noneSatisfy(exiled -> assertThat(exiled.card()).isSameAs(faceDown));
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(faceUp);
            assertThat(exiled.faceDown()).isFalse();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(faceUp, faceDown);
    }

    @Test
    void defendingPlayerCanChooseEmptyFaceUpPile() {
        addReadyToymaker();
        Card card = new Island();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCards).anySatisfy(exiled -> {
            assertThat(exiled.card()).isSameAs(card);
            assertThat(exiled.faceDown()).isTrue();
        });
        assertThat(gd.pileGroupingOrGuessCountThisTurn).isEqualTo(1);
    }

    @Test
    void opponentEndStepCountsEarlierPileGrouping() {
        addReadyToymaker();
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.handleMayAbilityChosen(player2, true);
        int lifeBeforeEndStep = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, lifeBeforeEndStep - 2);
    }

    @Test
    void opponentEndStepWithoutPileGroupingCausesNoLifeLoss() {
        addReadyToymaker();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyToymaker() {
        return addCreatureReady(player1, new TheCelestialToymaker());
    }

    @Test
    void attackLooksAtOnlyTheTopThreeCards() {
        addReadyToymaker();
        Card first = new Forest();
        Card second = new Island();
        Card third = new Swamp();
        Card fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second, third).doesNotContain(fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void defendingPlayerCanChooseEmptyFaceDownPile() {
        addReadyToymaker();
        Card first = new Forest();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.exiledCards).hasSize(2).allSatisfy(exiled -> assertThat(exiled.faceDown()).isFalse());
        int lifeBeforeEndStep = gd.playerLifeTotals.get(player2.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertLife(player2, lifeBeforeEndStep - 2);
    }

    @Test
    void pileGroupingDoesNotCarryOverToTheNextTurn() {
        addReadyToymaker();
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.setLibrary(player2, List.of(new Island(), new Swamp()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.handleMayAbilityChosen(player2, true);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        int lifeAfterFirstEndStep = gd.playerLifeTotals.get(player2.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertLife(player2, lifeAfterFirstEndStep);
        harness.assertLife(player1, 20);
    }
}
