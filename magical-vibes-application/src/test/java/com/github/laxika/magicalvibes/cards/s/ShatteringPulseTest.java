package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShatteringPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Shattering Pulse destroys target artifact without buyback")
    void destroysArtifactWithoutBuyback() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
        harness.assertInGraveyard(player1, "Shattering Pulse");
    }

    @Test
    @DisplayName("Paying buyback returns Shattering Pulse to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(5);

        UUID targetId = harness.getPermanentId(player2, "Rod of Ruin");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        assertThat(gd.playerHands.get(player1.getId()).stream().map(card -> card.getName()).toList())
                .containsExactly("Shattering Pulse");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Shattering Pulse cannot target a non-artifact")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
