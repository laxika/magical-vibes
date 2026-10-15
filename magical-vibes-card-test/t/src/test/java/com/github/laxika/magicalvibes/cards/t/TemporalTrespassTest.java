package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalTrespass.class, Forest.class})
class TemporalTrespassTest extends BaseCardTest {

    private void castTemporalTrespass() {
        harness.setHand(player1, List.of(new TemporalTrespass()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Resolving queues an extra turn and exiles Temporal Trespass")
    void resolvingQueuesExtraTurnAndExiles() {
        castTemporalTrespass();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Temporal Trespass"));
        harness.assertNotInGraveyard(player1, "Temporal Trespass");
    }

    @Test
    @DisplayName("The extra turn is taken by the caster before normal turn order resumes")
    void extraTurnIsTakenBeforeNormalTurnOrderResumes() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            int turnBefore = gd.turnNumber;

            castTemporalTrespass();

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player1.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);

            harness.forceStep(TurnStep.CLEANUP);
            harness.passBothPriorities();
            assertThat(gd.activePlayerId).isEqualTo(player2.getId());
            assertThat(gd.turnNumber).isEqualTo(turnBefore + 2);
        });
    }

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic mana")
    void delveExilesGraveyardCards() {
        List<Card> graveyard = List.of(
                new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TemporalTrespass()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1, 2, 3, 4, 5, 6, 7));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(graveyard);
        assertThat(gd.extraTurns).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertNotInGraveyard(player1, "Temporal Trespass");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(9);
    }

    private void prepareDelve(int graveyardSize, int blueMana, int genericMana) {
        harness.setGraveyard(player1, java.util.stream.IntStream.range(0, graveyardSize)
                .mapToObj(i -> (Card) new Forest()).toList());
        harness.setHand(player1, List.of(new TemporalTrespass()));
        harness.addMana(player1, ManaColor.BLUE, blueMana);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
    }

    private void castWithDelve(List<Integer> indices) {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, indices);
    }

    @Test
    @DisplayName("Delve can pay part of the generic cost while leaving unselected cards in the graveyard")
    void partialDelvePayment() {
        prepareDelve(5, 3, 5);
        List<Card> graveyard = List.copyOf(gd.playerGraveyards.get(player1.getId()));

        castWithDelve(List.of(0, 2, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyard.get(1), graveyard.get(3));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(graveyard.get(0), graveyard.get(2), graveyard.get(4));
        harness.passBothPriorities();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
        harness.assertNotInGraveyard(player1, "Temporal Trespass");
    }

    @Test
    @DisplayName("Delve cannot replace a required blue mana")
    void delveCannotPayColoredMana() {
        prepareDelve(8, 2, 1);

        assertThatThrownBy(() -> castWithDelve(List.of(0, 1, 2, 3, 4, 5, 6, 7)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Delve cannot exile more cards than the generic cost")
    void delveCannotExceedGenericCost() {
        prepareDelve(9, 3, 0);

        assertThatThrownBy(() -> castWithDelve(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(9);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A graveyard card cannot pay twice for delve")
    void delveCannotSelectTheSameCardTwice() {
        prepareDelve(8, 3, 0);

        assertThatThrownBy(() -> castWithDelve(List.of(0, 0, 2, 3, 4, 5, 6, 7)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
