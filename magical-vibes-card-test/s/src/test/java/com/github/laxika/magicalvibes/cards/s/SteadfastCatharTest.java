package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteadfastCathar.class})
class SteadfastCatharTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new SteadfastCathar());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Attacking gives it +0/+2 until end of turn")
    void attackBoostsToughnessUntilEndOfTurn() {
        Permanent cathar = addCreatureReady(player1, new SteadfastCathar());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cathar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cathar)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cathar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cathar)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger boosts only its source, not other Cathars")
    void attackBoostsOnlyItsSource() {
        Permanent attacker = addCreatureReady(player1, new SteadfastCathar());
        Permanent other = addCreatureReady(player1, new SteadfastCathar());
        Permanent opposing = addCreatureReady(player2, new SteadfastCathar());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking again in the same turn accumulates the toughness bonuses")
    void repeatedAttacksAccumulateUntilEndOfTurn() {
        Permanent cathar = addCreatureReady(player1, new SteadfastCathar());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gqs.getEffectiveToughness(gd, cathar)).isEqualTo(3);

        cathar.untap();
        cathar.setAttacking(false);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, cathar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cathar)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, cathar)).isEqualTo(1);
    }
}
