package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhastbarkTwins.class, GreenwoodSentinel.class})
class GhastbarkTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("Ghastbark Twins can block two creatures")
    void canBlockTwoCreatures() {
        Permanent twins = addCreatureReady(player2, new GhastbarkTwins());
        addAttackers(2);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(twins);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ghastbark Twins cannot block three creatures")
    void cannotBlockThreeCreatures() {
        addCreatureReady(player2, new GhastbarkTwins());
        addAttackers(3);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ))).isInstanceOf(IllegalStateException.class).hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Ghastbark Twins does not grant extra blocks to other creatures")
    void extraBlockAppliesOnlyToTwins() {
        addCreatureReady(player2, new GhastbarkTwins());
        addCreatureReady(player2, new GreenwoodSentinel());
        addAttackers(2);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)
        ))).isInstanceOf(IllegalStateException.class).hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Trample deals excess combat damage after assigning lethal damage")
    void tramplesOverBlocker() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GhastbarkTwins());
        Permanent blocker = addCreatureReady(player2, new GreenwoodSentinel());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 6)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Trample");
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Greenwood Sentinel");
        harness.assertOnBattlefield(player1, "Ghastbark Twins");
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new GreenwoodSentinel());
            attacker.setAttacking(true);
        }
    }
}
