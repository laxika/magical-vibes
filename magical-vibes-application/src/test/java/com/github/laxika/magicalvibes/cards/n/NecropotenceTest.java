package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NecropotenceTest extends BaseCardTest {

    // ===== Pay 1 life: exile top card face down, return at your next end step =====

    @Test
    @DisplayName("Pay 1 life exiles the top card face down and returns it at the controller's end step")
    void payOneLifeExilesTopCardAndReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 1 life paid, card exiled (not yet in hand), off the top of the library
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));

        // Advance to the controller's end step → the set-aside card returns to hand
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Set-aside card returns only at the controller's own end step, not an opponent's")
    void setAsideCardReturnsOnlyAtOwnEndStep() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card top = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(top);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // The opponent's end step must NOT return the card
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(top.getId()));

        // The controller's next end step returns it
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(top.getId()));
    }

    @Test
    @DisplayName("Two activations set aside two cards; both return at the end step")
    void multipleActivationsAllReturn() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);

        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(second);
        gd.playerDecks.get(player1.getId()).addFirst(first);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(first.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(first.getId()))
                .anyMatch(c -> c.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Empty library: paying 1 life exiles nothing")
    void emptyLibraryExilesNothing() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    // ===== Whenever you discard a card, exile that card from your graveyard =====

    @Test
    @DisplayName("A discarded card is exiled from the controller's graveyard, not left there")
    void discardedCardIsExiledFromGraveyard() {
        harness.addToBattlefield(player1, new Necropotence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Player1 casts Sift (draw 3, discard 1) — player1 is the discarding player
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, java.util.List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve Sift — draws 3, prompts for discard

        Card toDiscard = gd.playerHands.get(player1.getId()).get(0);
        harness.handleCardChosen(player1, 0);

        // The discarded card is exiled (Necropotence's trigger), not left in the graveyard
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(toDiscard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(toDiscard.getId()));
    }
}
