package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.r.Rancor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WolfPack.class, ForestBear.class, Rancor.class})
class WolfPackTest extends BaseCardTest {

    @Test
    @DisplayName("Blocked Wolf Pack can assign combat damage to defending player")
    void blockedWolfPackAssignsDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent wolfPack = addCreatureReady(player1, new WolfPack());
        wolfPack.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(wolfPack))));
        resolveCombat();

        // Assign all 7 damage to defending player (as though unblocked)
        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Blocked Wolf Pack can assign combat damage to blocker instead")
    void blockedWolfPackAssignsDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent wolfPack = addCreatureReady(player1, new WolfPack());
        wolfPack.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(wolfPack))));
        resolveCombat();

        // Assign all damage to blocker instead of defending player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        harness.assertNotOnBattlefield(player2, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Wolf Pack cannot split damage between its blocker and defending player")
    void cannotSplitDamageBetweenBlockerAndPlayer() {
        harness.setLife(player2, 20);
        Permanent wolfPack = addCreatureReady(player1, new WolfPack());
        wolfPack.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 5)))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 7));
        harness.assertLife(player2, 13);
        harness.assertOnBattlefield(player2, "Forest Bear");
        assertThat(wolfPack.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gaining trample does not prevent Wolf Pack from assigning all damage as though unblocked")
    void canAssignAllDamageToPlayerWithTrample() {
        harness.setLife(player2, 20);
        Permanent wolfPack = addCreatureReady(player1, new WolfPack());
        Permanent rancor = harness.addToBattlefieldAndReturn(player1, new Rancor());
        rancor.setAttachedTo(wolfPack.getId());
        wolfPack.setAttacking(true);
        addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(player2.getId(), 9));

        harness.assertLife(player2, 11);
        harness.assertOnBattlefield(player2, "Forest Bear");
        assertThat(wolfPack.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolf Pack retains the choice to assign no damage when its blocker leaves combat")
    void mayDeclineUnblockedAssignmentAfterBlockerLeaves() {
        harness.setLife(player2, 20);
        Permanent wolfPack = addCreatureReady(player1, new WolfPack());
        wolfPack.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ForestBear());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, blocker));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }
}
