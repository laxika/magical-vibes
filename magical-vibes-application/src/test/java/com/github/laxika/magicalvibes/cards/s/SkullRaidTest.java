package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkullRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two chosen cards")
    void discardsTwoCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Island(), new Island())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island())));
        castSkullRaid();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controller draws the difference when target has fewer than two cards")
    void controllerDrawsShortfall() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island())));
        castSkullRaid();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller draws two cards when target has an empty hand")
    void emptyTargetHandDrawsTwo() {
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island())));
        castSkullRaid();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Skull Raid cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SkullRaid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("Target must be an opponent");
    }

    private void castSkullRaid() {
        harness.setHand(player1, List.of(new SkullRaid()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());
    }
}
