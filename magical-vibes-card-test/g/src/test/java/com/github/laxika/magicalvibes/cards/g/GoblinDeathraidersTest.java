package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinDeathraiders.class, CylianElf.class})
class GoblinDeathraidersTest extends BaseCardTest {

    @Test
    @DisplayName("Goblin Deathraiders deals excess combat damage to defending player via trample")
    void trampleDealsExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent raiders = addCreatureReady(player1, new GoblinDeathraiders());
        raiders.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CylianElf());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Goblin Deathraiders is 3/1, blocker is 2/2 → assign lethal (2) to blocker, excess (1) to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Cylian Elf");
    }

    @Test
    @DisplayName("Trample may assign all combat damage to the blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinDeathraiders()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CylianElf());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Cylian Elf");
        harness.assertInGraveyard(player1, "Goblin Deathraiders");
    }

    @Test
    @DisplayName("Trample counts damage already marked on the blocker toward lethal damage")
    void markedDamageAllowsMoreTrampleDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinDeathraiders()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CylianElf());
        blocker.setMarkedDamage(1);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Cylian Elf");
        harness.assertInGraveyard(player1, "Goblin Deathraiders");
    }

    @Test
    @DisplayName("Trample cannot assign damage to the player before assigning lethal to the blocker")
    void rejectsInsufficientDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinDeathraiders()).setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CylianElf());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 1));
        harness.assertLife(player2, 19);
    }
}
