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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shriekdiver.class})
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
        return harness.addToBattlefieldAndReturn(player1, new Shriekdiver());
    }

    @Test
    @DisplayName("Haste is granted only on resolution and only to the source creature")
    void grantsHasteOnlyToSourceOnResolution() {
        Permanent source = addShriekdiver();
        Permanent other = addShriekdiver();
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Shriekdiver());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Its ability can be activated while tapped and does not untap it")
    void canActivateWhileTapped() {
        Permanent shriekdiver = addShriekdiver();
        shriekdiver.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, shriekdiver, Keyword.HASTE)).isTrue();
        assertThat(shriekdiver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires one mana")
    void cannotActivateWithoutMana() {
        Permanent shriekdiver = addShriekdiver();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.hasKeyword(gd, shriekdiver, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
