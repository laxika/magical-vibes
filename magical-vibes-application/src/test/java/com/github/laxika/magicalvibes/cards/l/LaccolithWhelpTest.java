package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SkyshroudRidgeback;
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

@CardUsed({LaccolithWhelp.class, SkyshroudRidgeback.class})
class LaccolithWhelpTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new LaccolithWhelp());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker() {
        return addCreatureReady(player2, new SkyshroudRidgeback());
    }

    private void declareBlock(Permanent attacker, Permanent blocker) {
        declareBlocks(attacker, List.of(blocker));
    }

    private void declareBlocks(Permanent attacker, List<Permanent> blockers) {
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        List<BlockerAssignment> assignments = blockers.stream()
                .map(blocker -> new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker), attackerIndex))
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Declining deals no damage and does not prevent combat damage")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Declining the ability lets the blocked creature deal combat damage")
    void decliningAbilityAllowsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, false);
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting the ability prevents the blocked creature from dealing combat damage")
    void acceptingAbilityPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Permanent otherCreature = addBlocker();

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());
        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage uses the attacker's current power when the ability resolves")
    void damageUsesCurrentPowerAtResolution() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        attacker.setPowerModifier(1);

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("The ability can target a creature other than the blocker")
    void canTargetAnotherCreature() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();
        Permanent otherCreature = addBlocker();

        declareBlock(attacker, blocker);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherCreature.getId());

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Multiple blockers still produce one ability")
    void multipleBlockersProduceOneAbility() {
        Permanent attacker = addAttacker();
        Permanent firstBlocker = addBlocker();
        Permanent secondBlocker = addBlocker();

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstBlocker.getId());

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Damage uses the source's last known power if it leaves before resolution")
    void damageUsesLastKnownPowerIfSourceLeavesBeforeResolution() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        attacker.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
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
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blocker.getId());

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
