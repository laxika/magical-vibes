package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AkroanCrusader;
import com.github.laxika.magicalvibes.cards.y.YokedOx;
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

@CardUsed({SatyrRambler.class, AkroanCrusader.class, YokedOx.class})
class SatyrRamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Satyr Rambler assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SatyrRambler());
        Permanent blocker = addCreatureReady(player2, new AkroanCrusader());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Trample requires lethal damage to the blocker before damage to the player")
    void cannotTrampleOverBlockerWithoutAssigningLethalDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SatyrRambler());
        Permanent blocker = addCreatureReady(player2, new YokedOx());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Trample");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        harness.assertLife(player2, 20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Yoked Ox");
    }

    @Test
    @DisplayName("A trampling attacker may assign all damage to its blocker")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new SatyrRambler());
        Permanent blocker = addCreatureReady(player2, new AkroanCrusader());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Akroan Crusader");
        harness.assertInGraveyard(player1, "Satyr Rambler");
    }
}
