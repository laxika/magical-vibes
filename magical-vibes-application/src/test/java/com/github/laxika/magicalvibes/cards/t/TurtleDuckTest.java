package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TurtleDuck.class)
class TurtleDuckTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sets base power to 4 and grants trample until end of turn")
    void abilitySetsPowerAndGrantsTrample() {
        Permanent turtleDuck = addReadyTurtleDuck();
        activateAbility();

        assertThat(gqs.getEffectivePower(gd, turtleDuck)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, turtleDuck)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, turtleDuck, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability's power and trample effect wear off at end of turn")
    void abilityWearsOffAtEndOfTurn() {
        Permanent turtleDuck = addReadyTurtleDuck();
        activateAbility();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, turtleDuck)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, turtleDuck)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, turtleDuck, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyTurtleDuck() {
        return addCreatureReady(player1, new TurtleDuck());
    }

    private void activateAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
