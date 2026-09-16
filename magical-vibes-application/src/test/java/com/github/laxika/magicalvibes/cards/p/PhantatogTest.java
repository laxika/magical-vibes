package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EarnestFellowship;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Phantatog.class, EarnestFellowship.class})
class PhantatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an enchantment gives Phantatog +1/+1 until end of turn")
    void sacrificeBoostsPhantatog() {
        Permanent phantatog = harness.addToBattlefieldAndReturn(player1, new Phantatog());
        harness.addToBattlefield(player1, new EarnestFellowship());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Earnest Fellowship");
        assertThat(phantatog.getPowerModifier()).isEqualTo(1);
        assertThat(phantatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding a card gives Phantatog +1/+1 until end of turn")
    void discardBoostsPhantatog() {
        Permanent phantatog = harness.addToBattlefieldAndReturn(player1, new Phantatog());
        harness.setHand(player1, List.of(new Phantatog()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Phantatog");
        assertThat(phantatog.getPowerModifier()).isEqualTo(1);
        assertThat(phantatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boosts wear off at cleanup")
    void boostsWearOffAtCleanup() {
        Permanent phantatog = harness.addToBattlefieldAndReturn(player1, new Phantatog());
        harness.addToBattlefield(player1, new EarnestFellowship());
        harness.setHand(player1, List.of(new Phantatog()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(phantatog.getPowerModifier()).isEqualTo(2);
        assertThat(phantatog.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(phantatog.getPowerModifier()).isEqualTo(0);
        assertThat(phantatog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot sacrifice an enchantment controlled by an opponent")
    void cannotSacrificeOpponentsEnchantment() {
        harness.addToBattlefield(player1, new Phantatog());
        harness.addToBattlefield(player2, new EarnestFellowship());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an enchantment");
    }

    @Test
    @DisplayName("Cannot activate without a matching cost")
    void cannotActivateWithoutCost() {
        harness.addToBattlefield(player1, new Phantatog());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an enchantment");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
