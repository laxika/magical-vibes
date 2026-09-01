package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrokersInitiate.class})
class BrokersInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a 5/5 when the ability is activated with green mana")
    void becomesFiveFiveWithGreenMana() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can pay the hybrid mana with blue mana")
    void becomesFiveFiveWithBlueMana() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(5);
    }

    @Test
    @DisplayName("The base power and toughness reset at end of turn")
    void resetsAtEndOfTurn() {
        Permanent initiate = addInitiateReady(player1);
        addActivationMana(player1, ManaColor.GREEN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, initiate)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, initiate)).isEqualTo(4);
    }

    private Permanent addInitiateReady(Player player) {
        Permanent permanent = new Permanent(new BrokersInitiate());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addActivationMana(Player player, ManaColor hybridColor) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, hybridColor, 1);
    }
}
