package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GideonsReproach.class, GrizzlyBears.class, GiantSpider.class, PrimordialWurm.class})
class GideonsReproachTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Gideon's Reproach targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Casting Gideon's Reproach targeting a blocking creature puts it on the stack")
    void castingTargetingBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blocker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Resolving deals 4 damage to attacking creature")
    void resolvingDeals4DamageToAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Grizzly Bears is 2/2, 4 damage kills it
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Resolving deals lethal damage to a creature with exactly four toughness")
    void resolvingKillsCreatureWithExactlyFourToughness() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        // Giant Spider is 2/4, 4 damage exactly kills it
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Gideon's Reproach goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Gideon's Reproach");
    }

    @Test
    @DisplayName("Gideon's Reproach fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Gideon's Reproach still goes to graveyard
        harness.assertInGraveyard(player2, "Gideon's Reproach");
    }

    @Test
    @DisplayName("An attacking creature with six toughness survives with four damage marked")
    void survivingAttackerHasExactlyFourDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Wurm");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Gideon's Reproach deals four damage to a blocking creature")
    void dealsFourDamageToBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blocker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("A target removed from combat before resolution takes no damage")
    void fizzlesIfTargetStopsAttacking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Wurm");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Gideon's Reproach");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
