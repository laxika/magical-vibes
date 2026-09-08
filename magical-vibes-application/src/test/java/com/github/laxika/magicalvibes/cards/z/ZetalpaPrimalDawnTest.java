package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZetalpaPrimalDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents non-flying creatures from blocking Zetalpa")
    void flyingPreventsGroundBlockers() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent zetalpa = addCreatureReady(player1, new ZetalpaPrimalDawn());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zetalpa);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Vigilance keeps Zetalpa untapped when it attacks")
    void vigilanceKeepsItUntapped() {
        Permanent zetalpa = addCreatureReady(player1, new ZetalpaPrimalDawn());

        declareAttackers(List.of(0));

        assertThat(zetalpa.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Indestructible lets Zetalpa survive lethal damage")
    void indestructibleSurvivesLethalDamage() {
        Permanent zetalpa = addCreatureReady(player1, new ZetalpaPrimalDawn());
        zetalpa.setMarkedDamage(8);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zetalpa);
    }

    @Test
    @DisplayName("Double strike and trample deal damage in both combat damage steps")
    void doubleStrikeAndTrampleDealDamageInBothSteps() {
        harness.setLife(player2, 20);
        Permanent zetalpa = addCreatureReady(player1, new ZetalpaPrimalDawn());
        Permanent blocker = addCreatureReady(player2, new EkunduGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
