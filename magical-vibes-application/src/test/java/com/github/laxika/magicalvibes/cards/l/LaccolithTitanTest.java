package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SkyshroudBehemoth;
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

@CardUsed({LaccolithTitan.class, SkyshroudBehemoth.class})
class LaccolithTitanTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new LaccolithTitan());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker() {
        return addCreatureReady(player2, new SkyshroudBehemoth());
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
    }

    @Test
    @DisplayName("Accepting deals power damage to a target creature and prevents combat damage")
    void acceptDealsPowerDamageAndPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMarkedDamage()).isEqualTo(6);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Declining deals no damage and does not prevent combat damage")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Damage uses the attacker's current power when the trigger resolves")
    void damageUsesCurrentPowerAtResolution() {
        Permanent attacker = addAttacker();
        Permanent blocker = addBlocker();

        declareBlock(attacker, blocker);
        harness.handlePermanentChosen(player1, blocker.getId());
        attacker.setPowerModifier(-1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Multiple blockers produce one becomes-blocked trigger")
    void multipleBlockersProduceOneTrigger() {
        Permanent attacker = addAttacker();
        Permanent firstBlocker = addBlocker();
        Permanent secondBlocker = addBlocker();

        declareBlocks(attacker, List.of(firstBlocker, secondBlocker));
        harness.handlePermanentChosen(player1, firstBlocker.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(6);
        assertThat(secondBlocker.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).containsExactly(attacker.getId());
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
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
