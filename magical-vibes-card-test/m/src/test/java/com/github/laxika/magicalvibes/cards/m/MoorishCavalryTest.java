package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoorishCavalry.class, Squire.class})
class MoorishCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Moorish Cavalry tramples over a blocking creature")
    void tramplesOverBlockingCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoorishCavalry());
        Permanent blocker = addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Squire"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
    @Test
    @DisplayName("Trample requires lethal damage to the blocker before player damage")
    void cannotAssignPlayerDamageBeforeLethalBlockerDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoorishCavalry());
        Permanent blocker = addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ))).isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1
        ));

        harness.assertInGraveyard(player2, "Squire");
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Trample allows assigning all damage to the blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new MoorishCavalry());
        Permanent blocker = addCreatureReady(player2, new Squire());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertInGraveyard(player2, "Squire");
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Moorish Cavalry");
    }
}
