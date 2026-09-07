package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Shriekdiver.class)
class ShriekdiverTest extends BaseCardTest {

    @Test
    @DisplayName("Activating its ability grants it haste")
    void grantsHaste() {
        Permanent shriekdiver = addShriekdiver();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shriekdiver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The granted haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent shriekdiver = addShriekdiver();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, shriekdiver, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shriekdiver, Keyword.HASTE)).isFalse();
    }

    private Permanent addShriekdiver() {
        harness.addToBattlefield(player1, new Shriekdiver());
        return findPermanent(player1, "Shriekdiver");
    }
}
