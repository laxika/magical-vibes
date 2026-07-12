package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSlumberTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Deep-Slumber Titan does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent titan = addReady(player1);
        titan.tap();

        advanceToNextTurn(player1);

        assertThat(titan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When Deep-Slumber Titan is dealt damage, it untaps (and survives as a 7/7)")
    void untapsWhenDealtDamage() {
        Permanent titan = addReady(player2);
        titan.tap();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID titanId = titan.getId();
        harness.castInstant(player1, 0, titanId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to the 7/7 Titan

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(titan.isTapped()).isTrue();

        harness.passBothPriorities(); // Resolve the untap trigger

        // Titan survives (7/7 takes 2 damage) and is now untapped
        harness.assertOnBattlefield(player2, "Deep-Slumber Titan");
        assertThat(titan.isTapped()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReady(Player player) {
        Permanent perm = new Permanent(new DeepSlumberTitan());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
