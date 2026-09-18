package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FurnaceWhelp.class)
class FurnaceWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gets +1/+0 until end of turn")
    void firebreathingBoostsPowerUntilEndOfTurn() {
        Permanent whelp = addCreatureReady(player1, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
    }

    @Test
    @DisplayName("Firebreathing activations stack during the turn")
    void firebreathingActivationsStack() {
        Permanent whelp = addCreatureReady(player1, new FurnaceWhelp());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, whelp)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whelp)).isEqualTo(2);
    }
}
