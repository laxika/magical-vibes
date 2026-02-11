package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HighGroundTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("High Ground has correct card properties")
    void hasCorrectProperties() {
        HighGround card = new HighGround();

        assertThat(card.getName()).isEqualTo("High Ground");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getStaticEffects()).hasSize(1);
        assertThat(card.getStaticEffects().getFirst())
                .isInstanceOf(GrantAdditionalBlockEffect.class);
        GrantAdditionalBlockEffect effect = (GrantAdditionalBlockEffect) card.getStaticEffects().getFirst();
        assertThat(effect.additionalBlocks()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting High Ground puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HighGround()));
        harness.addMana(player1, "W", 1);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("High Ground");
    }

    @Test
    @DisplayName("Resolving puts High Ground onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new HighGround()));
        harness.addMana(player1, "W", 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("High Ground"));
    }

    // ===== Blocking: one creature blocks two attackers =====

    @Test
    @DisplayName("Creature can block two attackers when High Ground is on the battlefield")
    void creatureCanBlockTwoAttackers() {
        harness.addToBattlefield(player2, new HighGround());

        // Player2 has one creature (blocker)
        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Player1 has two attacking creatures
        GrizzlyBears attacker1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(attacker1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears attacker2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(attacker2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Blocker is at index 1 (High Ground at 0), attackers at indices 0 and 1
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)
        ));

        assertThat(blockerPerm.isBlocking()).isTrue();
        assertThat(blockerPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Without High Ground, creature cannot block two attackers")
    void cannotBlockTwoWithoutHighGround() {
        // Player2 has one creature (blocker) but NO High Ground
        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Player1 has two attacking creatures
        GrizzlyBears attacker1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(attacker1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears attacker2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(attacker2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Blocker at index 0, attackers at indices 0 and 1
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

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears attacker = new GrizzlyBears();
        Permanent atkPerm = new Permanent(attacker);
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Blocker at index 1 (High Ground at 0), attacker at index 0
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

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        // Three attackers
        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Blocker at index 1 tries to block all 3 attackers — max is 2 (1 + 1 from High Ground)
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

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Blocker at index 2 (two High Grounds at 0, 1), attackers at 0, 1, 2
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

        // 4/4 blocker blocks two 2/2 attackers — blocker survives, both attackers die
        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(4);
        bigBlocker.setToughness(4);
        Permanent blockerPerm = new Permanent(bigBlocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        blockerPerm.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // 4/4 blocker deals 4 damage to first attacker (kills 2/2), remaining to second (kills 2/2)
        // Both 2/2 attackers deal 2+2=4 damage to blocker → 4/4 blocker dies
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Big blocker survives blocking two small attackers")
    void bigBlockerSurvivesTwoSmallAttackers() {
        harness.addToBattlefield(player2, new HighGround());

        // 5/5 blocker blocks two 1/1 attackers — blocker survives, both attackers die
        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blockerPerm = new Permanent(bigBlocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        blockerPerm.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears small1 = new GrizzlyBears();
        small1.setPower(1);
        small1.setToughness(1);
        Permanent atkPerm1 = new Permanent(small1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears small2 = new GrizzlyBears();
        small2.setPower(1);
        small2.setToughness(1);
        Permanent atkPerm2 = new Permanent(small2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Both 1/1 attackers should be dead
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        // 5/5 blocker takes only 2 damage total — survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Blocked attacker deals no damage to defending player even with multi-block")
    void blockedAttackerDealsNoDamageToPlayer() {
        harness.addToBattlefield(player2, new HighGround());
        harness.setLife(player2, 20);

        // Two 2/2 attackers, one blocked by a 2/2, one unblocked
        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0); // Blocks only first attacker
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        gs.passPriority(gd, player1);
        gs.passPriority(gd, player2);

        // Only unblocked attacker deals damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Effect stops when High Ground leaves =====

    @Test
    @DisplayName("Blocking limit reverts when High Ground is removed from battlefield")
    void effectStopsWhenRemoved() {
        harness.addToBattlefield(player2, new HighGround());

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        // Remove High Ground before declaring blockers
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("High Ground"));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Without High Ground, blocker at index 0 can only block one attacker
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
        // Player1 has High Ground, but player2 is the defender
        harness.addToBattlefield(player1, new HighGround());

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        GrizzlyBears atk1 = new GrizzlyBears();
        Permanent atkPerm1 = new Permanent(atk1);
        atkPerm1.setSummoningSick(false);
        atkPerm1.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm1);

        GrizzlyBears atk2 = new GrizzlyBears();
        Permanent atkPerm2 = new Permanent(atk2);
        atkPerm2.setSummoningSick(false);
        atkPerm2.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingBlockerDeclaration = true;

        // Player2 doesn't have High Ground — cannot multi-block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }
}
