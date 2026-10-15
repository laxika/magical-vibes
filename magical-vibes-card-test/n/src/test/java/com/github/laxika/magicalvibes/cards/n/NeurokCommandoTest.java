package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokCommando.class, SerraAngel.class, GoForTheThroat.class})
class NeurokCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Deals combat damage unblocked, accept may ability, draws a card")
    void drawsCardOnCombatDamageAccepted() {
        Permanent commando = addCreatureReady(player1, new NeurokCommando());
        commando.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Deals combat damage unblocked, decline may ability, no card drawn")
    void noDrawOnCombatDamageDeclined() {
        Permanent commando = addCreatureReady(player1, new NeurokCommando());
        commando.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("No trigger when Neurok Commando is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent commando = addCreatureReady(player1, new NeurokCommando());
        commando.setAttacking(true);

        // 4/4 blocker kills the 2/1 Commando
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Neurok Commando should be dead
        harness.assertInGraveyard(player1, "Neurok Commando");

        // No may ability prompt for combat damage (it didn't deal damage to a player)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Shroud prevents its controller from targeting Neurok Commando")
    void controllerCannotTargetCommando() {
        Permanent commando = harness.addToBattlefieldAndReturn(player1, new NeurokCommando());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, commando.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents an opponent from targeting Neurok Commando")
    void opponentCannotTargetCommando() {
        Permanent commando = harness.addToBattlefieldAndReturn(player2, new NeurokCommando());
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, commando.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("The attacking controller draws, rather than the damaged player")
    void otherControllerDrawsFromOwnLibrary() {
        Permanent commando = addCreatureReady(player2, new NeurokCommando());
        commando.setAttacking(true);
        harness.setLibrary(player2, List.of(new NeurokCommando(), new NeurokCommando()));
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(defenderHandBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
