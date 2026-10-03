package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CravenGiant.class, HonorGuard.class})
class CravenGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Craven Giant cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new CravenGiant());

        addCreatureReady(player1, new HonorGuard());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Craven Giant can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        addCreatureReady(player1, new CravenGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Craven Giant does not prevent another creature from blocking")
    void otherCreatureCanBlock() {
        addCreatureReady(player1, new HonorGuard());
        addCreatureReady(player2, new CravenGiant());
        addCreatureReady(player2, new HonorGuard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Craven Giant");
        harness.assertNotOnBattlefield(player1, "Honor Guard");
        harness.assertNotOnBattlefield(player2, "Honor Guard");
    }
}
