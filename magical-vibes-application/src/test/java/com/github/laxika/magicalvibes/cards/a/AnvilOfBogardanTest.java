package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnvilOfBogardan.class, Python.class})
class AnvilOfBogardanTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller keeps oversized hand during cleanup")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Opponent keeps oversized hand during cleanup while Anvil is on the battlefield")
    void opponentHasNoMaximumHandSize() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.setHand(player2, new ArrayList<>(List.of(
                new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Active player draws an additional card then discards during their draw step")
    void drawStepDrawThenDiscard() {
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.setHand(player1, new ArrayList<>(List.of(new Python(), new Python())));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        // Normal draw + Anvil draw = hand of 4, then discard prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opponent draws an additional card then discards during their draw step")
    void opponentDrawStepDrawThenDiscard() {
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.setHand(player2, new ArrayList<>(List.of(new Python(), new Python())));
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore - 2);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Anvil creates its own draw-and-discard trigger")
    void eachAnvilTriggersIndependently() {
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.addToBattlefield(player1, new AnvilOfBogardan());
        harness.setHand(player1, new ArrayList<>(List.of(new Python())));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A draw trigger resolves after Anvil leaves the battlefield")
    void triggerResolvesAfterAnvilLeaves() {
        var anvil = harness.addToBattlefieldAndReturn(player1, new AnvilOfBogardan());
        harness.setHand(player1, new ArrayList<>(List.of(new Python(), new Python())));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToDraw(player1);
        gd.playerBattlefields.get(player1.getId()).remove(anvil);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);
    }

    @Test
    @DisplayName("Removing Anvil restores the normal cleanup hand limit")
    void removingAnvilRestoresCleanupHandLimit() {
        var anvil = harness.addToBattlefieldAndReturn(player1, new AnvilOfBogardan());
        harness.setHand(player1, new ArrayList<>(List.of(
                new Python(), new Python(), new Python(), new Python(),
                new Python(), new Python(), new Python(), new Python()
        )));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        gd.playerBattlefields.get(player1.getId()).remove(anvil);
        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }
}
