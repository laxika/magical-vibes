package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FlowstoneCrusher;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LaccolithWarrior.class, FlowstoneCrusher.class})
class LaccolithWarriorTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new LaccolithWarrior());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker() {
        return addCreatureReady(player2, new FlowstoneCrusher());
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareBlocks(attacker, List.of(blocker));
    }

    private void declareBlocks(Permanent attacker, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIdx))
                .toList();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting deals power damage to a target creature and prevents combat damage")
    void acceptDealsPowerDamageAndPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Accepted ability prevents the attacker from dealing combat damage")
    void acceptedAbilityPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.handleMayAbilityChosen(player1, true);

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining deals no damage and does not prevent combat damage")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        harness.handlePermanentChosen(player1, blocker.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Damage uses the attacker's current power when the ability resolves")
    void damageUsesCurrentPowerAtResolution() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());
        attacker.setPowerModifier(-1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Multiple blockers produce one becomes-blocked ability")
    void multipleBlockersProduceOneTrigger() {
        Permanent attacker = addAttacker();
        Permanent firstBlocker = addBlocker();
        Permanent secondBlocker = addBlocker();

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstBlocker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(3);
        assertThat(secondBlocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Chooses the target when the trigger is put on the stack")
    void choosesTargetBeforeMayChoice() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, blocker.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("An unblocked attacker does not trigger")
    void unblockedDoesNotTrigger() {
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
