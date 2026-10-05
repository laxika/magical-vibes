package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NightMarketGuard.class})
class NightMarketGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Night Market Guard can block two attackers")
    void canBlockTwoAttackers() {
        Permanent guard = addCreatureReady(player2, new NightMarketGuard());
        addAttackers(2);
        prepareDeclareBlockers();

        int guardIdx = gd.playerBattlefields.get(player2.getId()).indexOf(guard);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(guardIdx, 0),
                new BlockerAssignment(guardIdx, 1)
        ));

        assertThat(guard.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Night Market Guard cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent guard = addCreatureReady(player2, new NightMarketGuard());
        addAttackers(3);
        prepareDeclareBlockers();

        int guardIdx = gd.playerBattlefields.get(player2.getId()).indexOf(guard);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(guardIdx, 0),
                new BlockerAssignment(guardIdx, 1),
                new BlockerAssignment(guardIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Another Night Market Guard does not increase this Guard's block capacity")
    void additionalGuardsDoNotGrantExtraBlocks() {
        Permanent guard = addCreatureReady(player2, new NightMarketGuard());
        addCreatureReady(player2, new NightMarketGuard());
        addAttackers(3);
        prepareDeclareBlockers();

        int guardIdx = gd.playerBattlefields.get(player2.getId()).indexOf(guard);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(guardIdx, 0),
                new BlockerAssignment(guardIdx, 1),
                new BlockerAssignment(guardIdx, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("A summoning-sick Night Market Guard can still block two attackers")
    void summoningSicknessDoesNotPreventAdditionalBlock() {
        Permanent guard = addCreatureReady(player2, new NightMarketGuard());
        guard.setSummoningSick(true);
        addAttackers(2);
        prepareDeclareBlockers();

        int guardIdx = gd.playerBattlefields.get(player2.getId()).indexOf(guard);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(guardIdx, 0),
                new BlockerAssignment(guardIdx, 1)
        ));

        assertThat(guard.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new NightMarketGuard());
            attacker.setAttacking(true);
        }
    }
}
