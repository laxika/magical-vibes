package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed(DwarvenVigilantes.class)
class DwarvenVigilantesTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new DwarvenVigilantes());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new DwarvenVigilantes());
    }

    private void advanceToUnblockedMay() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting: unblocked attacker deals power damage to chosen creature and assigns no combat damage")
    void acceptDealsPowerDamageAndPreventsCombatDamage() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The target may be a creature controlled by the attacking player")
    void canTargetOwnCreature() {
        Permanent attacker = addAttacker();
        Permanent victim = addCreatureReady(player1, new DwarvenVigilantes());

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("Damage uses the attacker's power when the ability resolves")
    void usesPowerAtResolution() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay();
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability uses the attacker's last known power if it leaves before resolution")
    void usesLastKnownPowerIfAttackerLeaves() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, attacker));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining: no damage to creatures and combat damage is not prevented")
    void declineDoesNothing() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Blocked attacker does not trigger")
    void blockedDoesNotTrigger() {
        Permanent attacker = addAttacker();
        Permanent blocker = addDefenderCreature();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
