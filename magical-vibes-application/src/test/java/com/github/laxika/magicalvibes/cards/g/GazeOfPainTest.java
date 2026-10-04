package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.p.PrismaticWard;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({BalduvianBears.class, GazeOfPain.class, GiantGrowth.class,
        DarkBanishing.class, PrismaticWard.class})
class GazeOfPainTest extends BaseCardTest {

    private void castGaze() {
        harness.setHand(player1, List.of(new GazeOfPain()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
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
        harness.castAndResolveInstant(player1, 0, victim.getId());

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

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        destroyAttacker(attacker);
        harness.passBothPriorities();
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
        harness.castAndResolveInstant(player1, 0, attacker.getId());

        destroyAttacker(attacker);
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

        prepareDeclareBlockers();

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

    private void destroyAttacker(Permanent attacker) {
        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }

    @Test
    @DisplayName("The delayed trigger chooses its target before either player can respond")
    void choosesTargetWhenTriggerGoesOnStack() {
        castGaze();
        addAttacker();
        Permanent victim = addDefenderCreature();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(victim.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prismatic Ward prevents the green attacker's damage even though Gaze of Pain is black")
    void preventionUsesAttackerColor() {
        castGaze();
        Permanent attacker = addAttacker();
        Permanent victim = addDefenderCreature();
        Permanent ward = new Permanent(new PrismaticWard());
        ward.setAttachedTo(victim.getId());
        ward.setChosenColor(CardColor.GREEN);
        gd.playerBattlefields.get(player2.getId()).add(ward);

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining lets the unblocked attacker deal normal combat damage")
    void decliningAllowsCombatDamage() {
        castGaze();
        addAttacker();
        addDefenderCreature();
        harness.setLife(player2, 20);

        advanceToUnblockedMay();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The delayed unblocked-attacker trigger expires at end of turn")
    void delayedTriggerExpiresAtEndOfTurn() {
        castGaze();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        addAttacker();
        addDefenderCreature();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }
}
