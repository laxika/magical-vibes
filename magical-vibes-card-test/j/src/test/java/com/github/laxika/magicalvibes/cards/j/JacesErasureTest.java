package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesErasure.class, Divination.class})
class JacesErasureTest extends BaseCardTest {

    @Test
    @DisplayName("Draw step trigger targets opponent before resolution and mills one card")
    void drawStepTriggersMillOpponent() {
        harness.addToBattlefield(player1, new JacesErasure());
        var topCard = gd.playerDecks.get(player2.getId()).getFirst();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToDraw(player1);
        chooseTriggerTarget(player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 1).contains(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting a trigger targeting self mills the controller one card")
    void drawStepTriggersMillSelf() {
        harness.addToBattlefield(player1, new JacesErasure());
        advanceToDraw(player1);
        int deckSizeAfterDraw = gd.playerDecks.get(player1.getId()).size();
        int graveyardSizeAfterDraw = gd.playerGraveyards.get(player1.getId()).size();

        chooseTriggerTarget(player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeAfterDraw - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeAfterDraw + 1);
    }

    @Test
    @DisplayName("The target is chosen even when the controller declines to mill at resolution")
    void declineDoesNotMill() {
        harness.addToBattlefield(player1, new JacesErasure());
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();
        int opponentGraveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToDraw(player1);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(opponentGraveyardBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Drawing two cards creates two independently targeted optional mill triggers")
    void spellDrawTriggersMayPrompt() {
        harness.addToBattlefield(player1, new JacesErasure());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        chooseTriggerTarget(player2.getId());
        chooseTriggerTarget(player2.getId());
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's draw does not trigger Jace's Erasure")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new JacesErasure());
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore - 1);
    }

    @Test
    @DisplayName("A player with an empty library remains a legal target and mills nothing")
    void emptyLibraryIsLegalTarget() {
        harness.addToBattlefield(player1, new JacesErasure());
        harness.setLibrary(player2, List.of());
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToDraw(player1);
        chooseTriggerTarget(player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void chooseTriggerTarget(UUID targetId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.handlePermanentChosen(player1, targetId);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.DRAW);
    }
}
