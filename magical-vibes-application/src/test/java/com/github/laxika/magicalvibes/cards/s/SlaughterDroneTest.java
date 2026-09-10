package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterDrone.class})
class SlaughterDroneTest extends BaseCardTest {

    @Test
    void activatingAbilityGrantsDeathtouchUntilEndOfTurn() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drone, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void grantedDeathtouchWearsOffAtEndOfTurn() {
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drone, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void cannotActivateAbilityWithOnlyColoredMana() {
        harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
