package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimianBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a land card gives Simian Brawler +1/+1 until end of turn")
    void discardingLandBoostsSimianBrawler() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        int basePower = gqs.getEffectivePower(gd, brawler);
        int baseToughness = gqs.getEffectiveToughness(gd, brawler);
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The +1/+1 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        harness.setHand(player1, List.of(new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(brawler.getPowerModifier()).isEqualTo(1);
        assertThat(brawler.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brawler.getPowerModifier()).isZero();
        assertThat(brawler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without a land card to discard")
    void cannotActivateWithoutLandCard() {
        harness.addToBattlefieldAndReturn(player1, new SimianBrawler());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
