package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrakhataPillarBugTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants lifelink until end of turn")
    void grantsLifelinkUntilEndOfTurn() {
        Permanent bug = addCreatureReady(player1, new PrakhataPillarBug());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bug, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Ability requires black mana")
    void requiresBlackMana() {
        addCreatureReady(player1, new PrakhataPillarBug());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
