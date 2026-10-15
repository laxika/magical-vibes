package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JazalGoldmane.class, BearCub.class})
class JazalGoldmaneTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts attacking creatures you control by the number of attacking creatures")
    void boostsAttackingCreaturesByAttackerCount() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        Permanent nonAttacker = addCreatureReady(player1, new BearCub());
        jazal.setAttacking(true);
        attacker.setAttacking(true);

        addManaForAbility();
        prepareForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        jazal.setAttacking(true);
        attacker.setAttacking(true);

        addManaForAbility();
        prepareForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(4);
    }

    @Test
    @DisplayName("Jazal need not attack for its ability to boost other attackers")
    void boostsAttackersWhileJazalIsNotAttacking() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        Permanent opponent = addCreatureReady(player2, new BearCub());
        attacker.setAttacking(true);

        addManaForAbility();
        prepareForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving before attackers are declared does not boost later attackers")
    void activationBeforeCombatDoesNotBoostLaterAttackers() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());

        addManaForAbility();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        jazal.setAttacking(true);
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attacking group and attacker count are determined at resolution")
    void countsOnlyCreaturesStillAttackingAtResolution() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        jazal.setAttacking(true);
        attacker.setAttacking(true);

        addManaForAbility();
        prepareForAbility();
        harness.activateAbility(player1, 0, null, null);
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The resolved boost remains fixed when the attacking group changes")
    void resolvedBoostDoesNotTrackLaterAttackerChanges() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new BearCub());
        Permanent laterAttacker = addCreatureReady(player1, new BearCub());
        jazal.setAttacking(true);
        attacker.setAttacking(true);

        addManaForAbility();
        prepareForAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.setAttacking(false);
        laterAttacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, jazal)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, jazal)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterAttacker)).isEqualTo(2);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void prepareForAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }
}
