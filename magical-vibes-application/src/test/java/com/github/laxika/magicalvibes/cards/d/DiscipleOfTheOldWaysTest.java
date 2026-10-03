package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DiscipleOfTheOldWays.class})
class DiscipleOfTheOldWaysTest extends BaseCardTest {

    @Test
    @DisplayName("Gains first strike until end of turn")
    void gainsFirstStrike() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        disciple.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isFalse();
    }
    @Test
    @DisplayName("Can activate while tapped and summoning sick, affecting only itself")
    void activatesWhileTappedAndSummoningSick() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DiscipleOfTheOldWays());
        disciple.setSummoningSick(true);
        disciple.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(disciple.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Green mana cannot pay the red activation cost")
    void requiresRedMana() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheOldWays());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, disciple, Keyword.FIRST_STRIKE)).isFalse();
    }
}
