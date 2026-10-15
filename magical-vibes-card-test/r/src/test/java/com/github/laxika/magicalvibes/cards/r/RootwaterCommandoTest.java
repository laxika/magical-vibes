package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootwaterCommando.class, GrizzlyBears.class, Island.class})
class RootwaterCommandoTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Rootwater Commando puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new RootwaterCommando(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Rootwater Commando onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new RootwaterCommando(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Rootwater Commando");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new RootwaterCommando()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Rootwater Commando enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new RootwaterCommando(), "{2}{U}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Rootwater Commando");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Rootwater Commando cannot be blocked when defending player controls an Island")
    void cannotBeBlockedWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new RootwaterCommando());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Rootwater Commando can be blocked when defending player does not control an Island")
    void canBeBlockedWhenDefenderDoesNotControlIsland() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new RootwaterCommando());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Rootwater Commando can be blocked when only the attacking player controls an Island")
    void canBeBlockedWhenOnlyAttackerControlsIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new RootwaterCommando());
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);
        declareAttackersAndPrepareBlockers(List.of(attackerIdx));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Rootwater Commando deals 2 damage to defending player")
    void dealsTwoDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = addCreatureReady(player1, new RootwaterCommando());
        atkPerm.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped Island still prevents Rootwater Commando from being blocked")
    void cannotBeBlockedWhenDefendersIslandIsTapped() {
        harness.addToBattlefield(player2, new Island());
        findPermanent(player2, "Island").tap();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new RootwaterCommando());
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk works when the second player attacks")
    void cannotBeBlockedWhenSecondPlayerAttacksIslandController() {
        harness.addToBattlefield(player1, new Island());
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new RootwaterCommando());
        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        declareAttackersAndPrepareBlockers(player2, List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
