package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlickshotVaultBuster.class, Shock.class})
class SlickshotVaultBusterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after its controller commits a crime")
    void getsBoostAfterCrime() {
        Permanent vaultBuster = harness.addToBattlefieldAndReturn(player1, new SlickshotVaultBuster());
        assertThat(gqs.getEffectivePower(gd, vaultBuster)).isEqualTo(1);

        castShockAt(player2.getId());

        assertThat(gqs.getEffectivePower(gd, vaultBuster)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vaultBuster)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost when its controller targets themself")
    void doesNotGetBoostFromTargetingSelf() {
        Permanent vaultBuster = harness.addToBattlefieldAndReturn(player1, new SlickshotVaultBuster());

        castShockAt(player1.getId());

        assertThat(gqs.getEffectivePower(gd, vaultBuster)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when the turn ends")
    void losesBoostAtTurnEnd() {
        Permanent vaultBuster = harness.addToBattlefieldAndReturn(player1, new SlickshotVaultBuster());
        castShockAt(player2.getId());
        assertThat(gqs.getEffectivePower(gd, vaultBuster)).isEqualTo(3);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vaultBuster)).isEqualTo(1);
    }

    private void castShockAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
