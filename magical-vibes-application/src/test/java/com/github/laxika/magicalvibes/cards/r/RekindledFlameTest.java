package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RekindledFlameTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Damage half =====

    @Test
    @DisplayName("Deals 4 damage to target player")
    void deals4DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RekindledFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 4 damage to target creature, destroying a 2/2")
    void deals4DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RekindledFlame()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Graveyard recursion trigger =====

    @Test
    @DisplayName("Offers to return from graveyard when an opponent has no cards in hand")
    void triggersWhenOpponentHandEmpty() {
        harness.setGraveyard(player1, List.of(new RekindledFlame()));
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the trigger's MayEffect from the stack

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Rekindled Flame");
    }

    @Test
    @DisplayName("Does not trigger when every opponent still has cards in hand")
    void doesNotTriggerWhenOpponentHasCards() {
        harness.setGraveyard(player1, List.of(new RekindledFlame()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting returns Rekindled Flame from graveyard to hand")
    void acceptReturnsToHand() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(flame.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(flame.getId()));
    }

    @Test
    @DisplayName("Declining keeps Rekindled Flame in the graveyard")
    void declineKeepsInGraveyard() {
        RekindledFlame flame = new RekindledFlame();
        harness.setGraveyard(player1, List.of(flame));
        harness.setHand(player2, List.of());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(flame.getId()));
    }
}
