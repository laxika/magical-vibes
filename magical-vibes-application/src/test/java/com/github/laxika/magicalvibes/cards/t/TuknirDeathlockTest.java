package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TuknirDeathlock.class, GrizzlyBears.class})
class TuknirDeathlockTest extends BaseCardTest {

    @Test
    void activationBoostsTargetCreatureAndTapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuknirDeathlock());
        source.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness + 2);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new TuknirDeathlock());
        source.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new TuknirDeathlock());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
