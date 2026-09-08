package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonWhisperer.class, GrizzlyBears.class})
class DragonWhispererTest extends BaseCardTest {

    @Test
    @DisplayName("Flying ability grants flying until end of turn")
    void flyingAbilityGrantsFlyingUntilEndOfTurn() {
        Permanent whisperer = addWhispererReady();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whisperer, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, whisperer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Pump ability gives Dragon Whisperer +1/+0 until end of turn")
    void pumpAbilityBoostsPower() {
        Permanent whisperer = addWhispererReady();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Formidable ability cannot be activated below total power eight")
    void formidableRequiresTotalPowerEight() {
        addWhispererReady();
        addBears(2);
        addFormidableMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power");
    }

    @Test
    @DisplayName("Formidable ability creates a 4/4 flying Dragon token")
    void formidableCreatesDragonToken() {
        addWhispererReady();
        addBears(3);
        addFormidableMana();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    private Permanent addWhispererReady() {
        return addCreatureReady(player1, new DragonWhisperer());
    }

    private void addBears(int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player1, new GrizzlyBears());
        }
    }

    private void addFormidableMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
