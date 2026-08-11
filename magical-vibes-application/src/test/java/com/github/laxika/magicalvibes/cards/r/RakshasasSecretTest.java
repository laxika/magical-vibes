package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RakshasasSecretTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards and mills two cards")
    void opponentDiscardsTwoAndMillsTwo() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        castRakshasasSecret(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        harness.assertInGraveyard(player1, "Rakshasa's Secret");
    }

    @Test
    @DisplayName("An empty opponent hand still results in two cards being milled")
    void emptyHandStillMillsTwo() {
        harness.setHand(player2, new ArrayList<>());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        castRakshasasSecret(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new RakshasasSecret()));
        addMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castRakshasasSecret(UUID targetId) {
        harness.setHand(player1, List.of(new RakshasasSecret()));
        addMana();
        harness.castSorcery(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
