package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GravestormTest extends BaseCardTest {

    @Test
    @DisplayName("The upkeep trigger only offers an opponent as a target")
    void upkeepTriggerTargetsOpponent() {
        harness.addToBattlefield(player1, new Gravestorm());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("The opponent can exile a card instead of allowing the controller to draw")
    void opponentExilesCard() {
        harness.addToBattlefield(player1, new Gravestorm());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Shock()));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
    }

    @Test
    @DisplayName("Declining or lacking a graveyard card offers the controller a draw")
    void controllerMayDrawWhenOpponentDoesNotExile() {
        harness.addToBattlefield(player1, new Gravestorm());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An empty opponent graveyard also allows the controller to draw")
    void emptyOpponentGraveyardOffersControllerDraw() {
        harness.addToBattlefield(player1, new Gravestorm());
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
    }
}
