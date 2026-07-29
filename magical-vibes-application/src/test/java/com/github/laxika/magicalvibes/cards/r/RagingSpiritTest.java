package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagingSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {2} makes it colorless until end of turn")
    void activatingMakesItColorless() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactly(CardColor.RED);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();
    }

    @Test
    @DisplayName("The colorless setting wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new RagingSpirit());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveColors(gd, spirit)).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, spirit)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot activate without paying the {2} cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new RagingSpirit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
