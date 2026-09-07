package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WirewoodPride.class, WirewoodElf.class, FountainOfYouth.class})
class WirewoodPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target by the number of Elves on the battlefield")
    void boostsByBattlefieldElfCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new WirewoodElf());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.setHand(player1, List.of(new WirewoodPride()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counts Elves controlled by both players")
    void countsElvesOnBothBattlefields() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        harness.addToBattlefield(player1, new WirewoodElf());
        harness.addToBattlefield(player2, new WirewoodElf());
        harness.setHand(player1, List.of(new WirewoodPride()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WirewoodElf());
        harness.setHand(player1, List.of(new WirewoodPride()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }
}
