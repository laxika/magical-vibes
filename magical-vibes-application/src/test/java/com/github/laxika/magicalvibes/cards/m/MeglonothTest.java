package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Meglonoth.class, GrizzlyBears.class, GiantGrowth.class, Unsummon.class, RayOfCommand.class})
class MeglonothTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking deals damage equal to power to the blocked creature's controller, not the creature")
    void blockingDealsPowerDamageToAttackerController() {
        Permanent meglonoth = addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(meglonoth.getId());

        harness.passBothPriorities();

        // 6 damage goes to the attacker's controller (a player) — the 2/2 attacker itself is unharmed.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Block trigger is non-targeting (cannot be fizzled)")
    void blockTriggerIsNonTargeting() {
        addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Meglonoth attacks without tapping or triggering its block ability")
    void attacksWithoutTappingOrBlockTrigger() {
        Permanent meglonoth = addCreatureReady(player1, new Meglonoth());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(meglonoth.isAttacking()).isTrue();
        assertThat(meglonoth.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Meglonoth tramples over a smaller blocker")
    void tramplesOverSmallerBlocker() {
        addCreatureReady(player1, new Meglonoth());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
        harness.assertOnBattlefield(player1, "Meglonoth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Block damage uses Meglonoth's power at resolution")
    void blockDamageUsesPowerAtResolution() {
        Permanent meglonoth = addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player2, 0, meglonoth.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Block damage still happens after the blocked creature leaves")
    void blockedCreatureLeavingDoesNotStopDamage() {
        addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A departing Meglonoth uses its power immediately before leaving")
    void departingMeglonothUsesLastKnownPower() {
        Permanent meglonoth = addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new GiantGrowth(), new Unsummon()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player2, 0, meglonoth.getId());
        harness.castAndResolveInstant(player2, 0, meglonoth.getId());
        harness.assertInHand(player2, "Meglonoth");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A departing blocked creature's last controller receives the damage")
    void departingBlockedCreatureUsesLastKnownController() {
        addCreatureReady(player2, new Meglonoth());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setHand(player2, List.of(new RayOfCommand(), new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
    }
}
