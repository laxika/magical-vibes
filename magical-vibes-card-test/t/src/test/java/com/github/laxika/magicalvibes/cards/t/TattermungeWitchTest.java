package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TattermungeWitch.class, SafeholdElite.class})
class TattermungeWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Only blocked creatures get +1/+0 and trample")
    void buffsOnlyBlockedCreatures() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent blockedAttacker = addCreatureReady(player1, new SafeholdElite());
        blockedAttacker.setAttacking(true);
        Permanent bystander = addCreatureReady(player1, new SafeholdElite());
        Permanent blocker = addCreatureReady(player2, new SafeholdElite());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blockedAttacker.getId());

        activateWitch(witch);

        // The blocked attacker gets the buff.
        assertThat(gqs.getEffectivePower(gd, blockedAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blockedAttacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blockedAttacker, Keyword.TRAMPLE)).isTrue();

        // A non-attacking bystander is not blocked → unaffected.
        assertThat(gqs.getEffectivePower(gd, bystander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bystander, Keyword.TRAMPLE)).isFalse();

        // The blocker is blocking, not blocked → unaffected.
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An unblocked attacker is not buffed")
    void unblockedAttackerNotBuffed() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent unblockedAttacker = addCreatureReady(player1, new SafeholdElite());
        unblockedAttacker.setAttacking(true);

        activateWitch(witch);

        assertThat(gqs.getEffectivePower(gd, unblockedAttacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, unblockedAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The buff wears off at end of turn")
    void buffWearsOffAtEndOfTurn() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent blockedAttacker = addCreatureReady(player1, new SafeholdElite());
        blockedAttacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SafeholdElite());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blockedAttacker.getId());

        activateWitch(witch);

        assertThat(gqs.getEffectivePower(gd, blockedAttacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blockedAttacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, blockedAttacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blockedAttacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A blocked attacker still gets the buff after its blocker leaves combat")
    void buffsAttackerWhoseBlockerLeftCombat() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent attacker = addCreatureReady(player1, new SafeholdElite());
        Permanent blocker = addCreatureReady(player2, new SafeholdElite());
        declareAttackersAndPrepareBlockers(java.util.List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, java.util.List.of(new BlockerAssignment(0, 1))));
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(attacker.getId());

        harness.getPermanentRemovalService().removePermanentToExile(gd, blocker);
        activateWitch(witch);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The defending player's Witch also buffs opposing blocked attackers")
    void buffsOpposingBlockedAttacker() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent attacker = addCreatureReady(player2, new SafeholdElite());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new SafeholdElite());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witch), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack the power bonus")
    void repeatedActivationsStack() {
        Permanent witch = addCreatureReady(player1, new TattermungeWitch());
        Permanent attacker = addCreatureReady(player1, new SafeholdElite());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SafeholdElite());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());

        activateWitch(witch);
        activateWitch(witch);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(witch.isTapped()).isFalse();
    }

    private void activateWitch(Permanent witch) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(witch);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }
}
