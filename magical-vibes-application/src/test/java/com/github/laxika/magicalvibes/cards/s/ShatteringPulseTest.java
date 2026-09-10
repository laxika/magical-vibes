package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteringPulse.class, Spellbook.class, RagingGoblin.class})
class ShatteringPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Shattering Pulse destroys target artifact without buyback")
    void destroysArtifactWithoutBuyback() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Shattering Pulse");
    }

    @Test
    @DisplayName("Paying buyback returns Shattering Pulse to its owner's hand")
    void buybackReturnsToHand() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(5);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.castInstantWithBuyback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player1, "Shattering Pulse");
        harness.assertNotInGraveyard(player1, "Shattering Pulse");
    }

    @Test
    @DisplayName("Buyback does not return Shattering Pulse when its target is gone at resolution")
    void buybackDoesNotReturnWhenTargetIsGoneAtResolution() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(5);

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.castInstantWithBuyback(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shattering Pulse");
        harness.assertNotInHand(player1, "Shattering Pulse");
    }

    @Test
    @DisplayName("Shattering Pulse cannot target a non-artifact")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new ShatteringPulse()));
        addMana(2);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(int amount) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, amount - 1);
    }
}
