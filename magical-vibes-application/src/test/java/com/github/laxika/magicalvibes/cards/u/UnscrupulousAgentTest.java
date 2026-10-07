package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnscrupulousAgent.class, GrizzlyBears.class, Forest.class, Murder.class})
class UnscrupulousAgentTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the target opponent choose a card to exile")
    void opponentChoosesCardToExile() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        castAgent(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).getFirst().getName()).isEqualTo("Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty target hand produces no exile choice")
    void emptyTargetHandProducesNoChoice() {
        harness.setHand(player2, new ArrayList<>());
        castAgent(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new UnscrupulousAgent()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("The opponent may choose a land and exiles exactly one card")
    void opponentCanChooseLand() {
        harness.setHand(player2, List.of(new UnscrupulousAgent(), new Forest()));
        castAgent(player2.getId());

        harness.handleCardChosen(player2, 1);

        harness.assertInHand(player2, "Unscrupulous Agent");
        harness.assertNotInHand(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The exiled card does not return when the Agent dies")
    void exiledCardStaysExiledWhenAgentDies() {
        harness.setHand(player2, List.of(new Forest()));
        castAgent(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Unscrupulous Agent"));

        harness.assertInGraveyard(player1, "Unscrupulous Agent");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
    }

    @Test
    @DisplayName("The ETB still exiles a card if the Agent dies before it resolves")
    void triggerResolvesAfterAgentDies() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new UnscrupulousAgent(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Unscrupulous Agent"));
        harness.assertInGraveyard(player1, "Unscrupulous Agent");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAgent(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new UnscrupulousAgent()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
