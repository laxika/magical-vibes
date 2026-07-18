package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TempestEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay 10 life exchanges the revealed card for Tempest Efreet")
    void declineExchangesCards() {
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addCreatureReady(player1, new TempestEfreet());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities(); // resolve ability -> may-pay prompt for player2

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // Revealed card goes to the ability controller's hand; the opponent's hand is emptied.
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Tempest Efreet (sacrificed as a cost) moves from its controller's graveyard to the opponent's.
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Tempest Efreet"));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getName().equals("Tempest Efreet"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying 10 life keeps the card and leaves Tempest Efreet in its controller's graveyard")
    void payingKeepsCards() {
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        addCreatureReady(player1, new TempestEfreet());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // No exchange: Tempest Efreet stays where the sacrifice put it (its controller's graveyard).
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Tempest Efreet"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Tempest Efreet"));
    }

    @Test
    @DisplayName("An opponent who can't pay 10 life exchanges automatically with no prompt")
    void cannotPayExchangesAutomatically() {
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 5);
        addCreatureReady(player1, new TempestEfreet());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Tempest Efreet"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining with an empty hand exchanges nothing")
    void emptyHandExchangesNothing() {
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());
        addCreatureReady(player1, new TempestEfreet());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Nothing to reveal, so Tempest Efreet is not exchanged — it stays in its controller's graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Tempest Efreet"));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getName().equals("Tempest Efreet"));
    }

    @Test
    @DisplayName("The ability cannot target the activating player")
    void cannotTargetSelf() {
        addCreatureReady(player1, new TempestEfreet());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
