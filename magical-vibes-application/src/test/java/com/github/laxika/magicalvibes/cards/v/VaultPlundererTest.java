package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaultPlunderer.class, GrizzlyBears.class})
class VaultPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes the target opponent draw a card and lose 1 life")
    void targetsOpponent() {
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        castVaultPlunderer(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB can target its controller")
    void targetsController() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castVaultPlunderer(player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VaultPlunderer()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
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
