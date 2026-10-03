package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsultTheNecrosages.class, GrayscaledGharial.class, Island.class})
class ConsultTheNecrosagesTest extends BaseCardTest {

    @Test
    @DisplayName("Draw mode makes the target player draw two cards")
    void drawModeMakesTargetPlayerDrawTwo() {
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    @Test
    @DisplayName("Draw mode can target the caster")
    void drawModeCanTargetCaster() {
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    @Test
    @DisplayName("Discard mode makes the target player discard two cards")
    void discardModeMakesTargetPlayerDiscardTwo() {
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.setHand(player2, List.of(new Island(), new Island(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    @Test
    @DisplayName("Both modes reject a permanent as a target")
    void rejectsPermanentTarget() {
        harness.addToBattlefield(player2, new GrayscaledGharial());
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        addMana();

        UUID permanentId = harness.getPermanentId(player2, "Grayscaled Gharial");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discard mode can target the caster, who chooses the cards")
    void discardModeCanTargetCaster() {
        Island keptCard = new Island();
        GrayscaledGharial firstDiscard = new GrayscaledGharial();
        Island secondDiscard = new Island();
        harness.setHand(player1, List.of(new ConsultTheNecrosages(), keptCard, firstDiscard, secondDiscard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player1.getId());
        harness.handleCardChosen(player1, 1);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    @Test
    @DisplayName("Discard mode discards the only card in a one-card hand")
    void discardModeWithOneCardInHand() {
        Island discardedCard = new Island();
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.setHand(player2, List.of(discardedCard));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    @Test
    @DisplayName("Discard mode can target a player with an empty hand")
    void discardModeWithEmptyHand() {
        harness.setHand(player1, List.of(new ConsultTheNecrosages()));
        harness.setHand(player2, List.of());
        addMana();

        harness.castAndResolveSorcery(player1, 0, 1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Consult the Necrosages");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
