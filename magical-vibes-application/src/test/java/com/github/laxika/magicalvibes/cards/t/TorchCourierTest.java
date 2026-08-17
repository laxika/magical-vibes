package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TorchCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Torch Courier gives another target creature haste")
    void sacrificingCourierGivesAnotherCreatureHaste() {
        Permanent courier = addCreatureReady(player1, new TorchCourier());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courier);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TorchCourier());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Torch Courier itself")
    void cannotTargetItself() {
        Permanent courier = addCreatureReady(player1, new TorchCourier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, courier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
