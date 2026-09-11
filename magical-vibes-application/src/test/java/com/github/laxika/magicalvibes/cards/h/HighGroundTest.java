package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighGround.class, GrizzlyBears.class})
class HighGroundTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting High Ground puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HighGround()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts High Ground onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new HighGround()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof HighGround);
    }

    // ===== Blocking: one creature blocks two attackers =====

    @Test
    @DisplayName("Creature can block two attackers when High Ground is on the battlefield")
    void creatureCanBlockTwoAttackers() {
        harness.addToBattlefield(player2, new HighGround());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(blockerPerm.isBlocking()).isTrue();
        assertThat(blockerPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("High Ground lets each creature you control block an additional creature")
    void eachControlledCreatureCanBlockAnAdditionalCreature() {
        harness.addToBattlefield(player2, new HighGround());
        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);
        Permanent attacker3 = addCreatureReady(player1, new GrizzlyBears());
        attacker3.setAttacking(true);
        Permanent attacker4 = addCreatureReady(player1, new GrizzlyBears());
        attacker4.setAttacking(true);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        List<Permanent> attackerBattlefield = gd.playerBattlefields.get(player1.getId());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(defenderBattlefield.indexOf(blocker1), attackerBattlefield.indexOf(attacker1)),
                new BlockerAssignment(defenderBattlefield.indexOf(blocker1), attackerBattlefield.indexOf(attacker2)),
                new BlockerAssignment(defenderBattlefield.indexOf(blocker2), attackerBattlefield.indexOf(attacker3)),
                new BlockerAssignment(defenderBattlefield.indexOf(blocker2), attackerBattlefield.indexOf(attacker4))
        ));

        assertThat(blocker1.getBlockingTargets()).containsExactlyInAnyOrder(
                attackerBattlefield.indexOf(attacker1), attackerBattlefield.indexOf(attacker2));
        assertThat(blocker2.getBlockingTargets()).containsExactlyInAnyOrder(
                attackerBattlefield.indexOf(attacker3), attackerBattlefield.indexOf(attacker4));
    }

    @Test
    @DisplayName("Without High Ground, creature cannot block two attackers")
    void cannotBlockTwoWithoutHighGround() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Cannot assign same blocker to same attacker twice")
    void cannotBlockSameAttackerTwice() {
        harness.addToBattlefield(player2, new HighGround());

        addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate blocker-attacker pair");
    }

    @Test
    @DisplayName("Cannot block three attackers with one creature and only one High Ground")
    void cannotExceedMaxBlocks() {
        harness.addToBattlefield(player2, new HighGround());

        addCreatureReady(player2, new GrizzlyBears());

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1),
                new BlockerAssignment(1, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Two High Grounds allow a creature to block three attackers")
    void twoHighGroundsAllowThreeBlocks() {
        harness.addToBattlefield(player2, new HighGround());
        harness.addToBattlefield(player2, new HighGround());

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = addCreatureReady(player1, new GrizzlyBears());
            atkPerm.setAttacking(true);
        }

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(2, 0),
                new BlockerAssignment(2, 1),
                new BlockerAssignment(2, 2)
        ));

        assertThat(blockerPerm.isBlocking()).isTrue();
        assertThat(blockerPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    // ===== Combat damage with multi-block =====

    @Test
    @DisplayName("Blocker blocking two attackers deals damage to both and takes damage from both")
    void blockerDealsDamageToBothAttackers() {
        harness.addToBattlefield(player2, new HighGround());

        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(4);
        bigBlocker.setToughness(4);
        Permanent blockerPerm = addCreatureReady(player2, bigBlocker);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        blockerPerm.addBlockingTarget(1);

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        resolveCombat();

        harness.handleCombatDamageAssigned(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm),
                java.util.Map.of(atkPerm1.getId(), 2, atkPerm2.getId(), 2));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blockerPerm.getCard());
    }

    @Test
    @DisplayName("Big blocker survives blocking two small attackers")
    void bigBlockerSurvivesTwoSmallAttackers() {
        harness.addToBattlefield(player2, new HighGround());

        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blockerPerm = addCreatureReady(player2, bigBlocker);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        blockerPerm.addBlockingTarget(1);

        GrizzlyBears small1 = new GrizzlyBears();
        small1.setPower(1);
        small1.setToughness(1);
        Permanent atkPerm1 = addCreatureReady(player1, small1);
        atkPerm1.setAttacking(true);

        GrizzlyBears small2 = new GrizzlyBears();
        small2.setPower(1);
        small2.setToughness(1);
        Permanent atkPerm2 = addCreatureReady(player1, small2);
        atkPerm2.setAttacking(true);

        resolveCombat();

        harness.handleCombatDamageAssigned(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm),
                java.util.Map.of(atkPerm1.getId(), 4, atkPerm2.getId(), 1));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blockerPerm);
    }

    @Test
    @DisplayName("Blocked attacker deals no damage to defending player even with multi-block")
    void blockedAttackerDealsNoDamageToPlayer() {
        harness.addToBattlefield(player2, new HighGround());
        harness.setLife(player2, 20);

        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0); // Blocks only first attacker

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Effect stops when High Ground leaves =====

    @Test
    @DisplayName("Blocking limit reverts when High Ground is removed from battlefield")
    void effectStopsWhenRemoved() {
        Permanent highGround = harness.addToBattlefieldAndReturn(player2, new HighGround());

        addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        gd.playerBattlefields.get(player2.getId()).remove(highGround);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    // ===== High Ground only affects its controller =====

    @Test
    @DisplayName("High Ground does not grant additional blocks to opponent's creatures")
    void doesNotAffectOpponent() {
        harness.addToBattlefield(player1, new HighGround());

        addCreatureReady(player2, new GrizzlyBears());

        Permanent atkPerm1 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm1.setAttacking(true);

        Permanent atkPerm2 = addCreatureReady(player1, new GrizzlyBears());
        atkPerm2.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }
}

