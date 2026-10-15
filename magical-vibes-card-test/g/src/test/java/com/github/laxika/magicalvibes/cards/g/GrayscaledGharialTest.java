package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WateryGrave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrayscaledGharial.class, BorosRecruit.class, Island.class, WateryGrave.class})
class GrayscaledGharialTest extends BaseCardTest {
    @Test
    @DisplayName("Grayscaled Gharial cannot be blocked when defending player controls a tapped nonbasic Island")
    void cannotBeBlockedWhenDefenderControlsTappedNonbasicIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new WateryGrave());
        island.tap();

        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Grayscaled Gharial cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Grayscaled Gharial can be blocked when defending player does not control an Island")
    void canBeBlockedWhenDefenderDoesNotControlIsland() {
        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Grayscaled Gharial can be blocked when only the attacking player controls an Island")
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent blocker = addCreatureReady(player2, new BorosRecruit());
        Permanent attacker = addCreatureReady(player1, new GrayscaledGharial());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
