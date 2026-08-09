package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlowstoneThopterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives +1/-1 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }
}
