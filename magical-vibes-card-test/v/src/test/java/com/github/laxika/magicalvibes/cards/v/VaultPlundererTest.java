package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaultPlunderer.class})
class VaultPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the target opponent draw a card and lose 1 life")
    void targetsOpponent() {
        harness.setLibrary(player2, List.of(new VaultPlunderer()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        castVaultPlunderer(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player2, "Vault Plunderer");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void targetsController() {
        harness.setLibrary(player1, List.of(new VaultPlunderer()));
        castVaultPlunderer(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Vault Plunderer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("ETB cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VaultPlunderer());
        harness.setHand(player1, List.of(new VaultPlunderer()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB waits for resolution and still resolves after its source leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.setLibrary(player2, List.of(new VaultPlunderer()));
        harness.setHand(player1, List.of(new VaultPlunderer()));
        addMana();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vault Plunderer");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        harness.assertLife(player2, 20);

        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player2, "Vault Plunderer");
        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Vault Plunderer");
    }

    private void castVaultPlunderer(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new VaultPlunderer()));
        addMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
