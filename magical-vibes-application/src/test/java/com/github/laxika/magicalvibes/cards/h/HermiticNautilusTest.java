package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HermiticNautilus.class)
class HermiticNautilusTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives this creature +3/-3 until end of turn")
    void activationBoostsSelf() {
        Permanent nautilus = addReadyNautilus();
        int basePower = gqs.getEffectivePower(gd, nautilus);
        int baseToughness = gqs.getEffectiveToughness(gd, nautilus);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nautilus)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, nautilus)).isEqualTo(baseToughness - 3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent nautilus = addReadyNautilus();
        int basePower = gqs.getEffectivePower(gd, nautilus);
        int baseToughness = gqs.getEffectiveToughness(gd, nautilus);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, nautilus)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, nautilus)).isEqualTo(baseToughness);
    }

    private Permanent addReadyNautilus() {
        Permanent nautilus = harness.addToBattlefieldAndReturn(player1, new HermiticNautilus());
        nautilus.setSummoningSick(false);
        return nautilus;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
