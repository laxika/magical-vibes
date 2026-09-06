package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BalduvianBears.class, GazeOfPain.class, GiantGrowth.class})
class GazeOfPainTest extends BaseCardTest {

    private void castGaze() {
        harness.setHand(player1, List.of(new GazeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderCreature() {
        return addCreatureReady(player2, new BalduvianBears());
    }

    private void advanceToUnblockedMay() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting: unblocked attacker deals power damage to chosen creature and assigns no combat damage")
    void acceptDealsPowerDamageAndPreventsCombatDamage() {
        castGaze();
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
    @DisplayName("Accepting: the attacker deals no damage in the following combat-damage step")
    void acceptedAbilityPreventsFollowingCombatDamage() {
        castGaze();
        addAttacker();
        Permanent victim = addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each unblocked creature gets its own may ability")
    void eachUnblockedAttackerGetsItsOwnMayAbility() {
        castGaze();
        Permanent firstAttacker = addAttacker();
        Permanent secondAttacker = addAttacker();
        Permanent victim = addDefenderCreature();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage)
                .contains(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @DisplayName("Accepting after the attacker leaves uses its last known power")
    void acceptedAbilityUsesLastKnownAttackerPower() {
        castGaze();
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay();
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting after a pumped attacker leaves uses its last known power")
    void acceptedAbilityUsesLastKnownPumpedAttackerPower() {
        castGaze();
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining: no damage to creatures and combat damage is not prevented")
    void declineDoesNothing() {
        castGaze();
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Blocked attacker does not trigger Gaze of Pain")
    void blockedDoesNotTrigger() {
        castGaze();
        Permanent attacker = addAttacker();
        Permanent blocker = addDefenderCreature();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("An unblocked creature controlled by the opponent does not trigger Gaze of Pain")
    void opponentControlledAttackerDoesNotTrigger() {
        castGaze();
        Permanent attacker = addCreatureReady(player2, new BalduvianBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        castGaze();
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
