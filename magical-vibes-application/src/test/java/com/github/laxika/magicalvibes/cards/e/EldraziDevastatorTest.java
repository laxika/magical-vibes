package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
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

@CardUsed({EldraziDevastator.class, GoblinHero.class})
class EldraziDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Eldrazi Devastator's trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent devastator = addCreatureReady(player1, new EldraziDevastator());
        Permanent blocker = addCreatureReady(player2, new GoblinHero());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 6
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(devastator);
    }

    @Test
    @DisplayName("Trample requires lethal damage to every blocker before damaging the player")
    void requiresLethalDamageToEveryBlocker() {
        harness.setLife(player2, 20);
        Permanent devastator = addCreatureReady(player1, new EldraziDevastator());
        Permanent firstBlocker = addCreatureReady(player2, new GoblinHero());
        Permanent secondBlocker = addCreatureReady(player2, new GoblinHero());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 1,
                player2.getId(), 5
        ))).isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 2,
                secondBlocker.getId(), 2,
                player2.getId(), 4
        ));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstBlocker, secondBlocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(devastator);
    }

    @Test
    @DisplayName("Trample allows all damage to be assigned to a blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent devastator = addCreatureReady(player1, new EldraziDevastator());
        Permanent blocker = addCreatureReady(player2, new GoblinHero());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 8));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(devastator);
    }

    @Test
    @DisplayName("Trample cannot deal damage to the player when the blocker survives all assigned damage")
    void noExcessDamageAgainstLargerBlocker() {
        harness.setLife(player2, 20);
        Permanent devastator = addCreatureReady(player1, new EldraziDevastator());
        Permanent blocker = addCreatureReady(player2, new EldraziDevastator());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 8));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(devastator);
    }
}
