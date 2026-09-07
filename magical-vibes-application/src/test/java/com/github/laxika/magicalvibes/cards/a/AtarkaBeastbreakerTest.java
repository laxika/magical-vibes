package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtarkaBeastbreaker.class, GrizzlyBears.class})
class AtarkaBeastbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void formidableRequiresTotalPowerEight() {
        addBeastbreakerReady();
        addBears(2);
        addFormidableMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable ability gives Atarka Beastbreaker +4/+4 until end of turn")
    void formidableBoostsUntilEndOfTurn() {
        Permanent beastbreaker = addBeastbreakerReady();
        addBears(3);
        addFormidableMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beastbreaker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastbreaker)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beastbreaker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beastbreaker)).isEqualTo(2);
    }

    private Permanent addBeastbreakerReady() {
        return addCreatureReady(player1, new AtarkaBeastbreaker());
    }

    private void addBears(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }

    private void addFormidableMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
