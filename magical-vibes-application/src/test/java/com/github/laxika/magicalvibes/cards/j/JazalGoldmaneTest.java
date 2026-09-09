package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JazalGoldmane.class, GrizzlyBears.class})
class JazalGoldmaneTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts attacking creatures you control by the number of attacking creatures")
    void boostsAttackingCreaturesByAttackerCount() {
        Permanent jazal = addCreatureReady(player1, new JazalGoldmane());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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
