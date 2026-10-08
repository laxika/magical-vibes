package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DouserOfLights;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorchCourier.class, DouserOfLights.class})
class TorchCourierTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Torch Courier gives another target creature haste")
    void sacrificingCourierGivesAnotherCreatureHaste() {
        Permanent courier = addCreatureReady(player1, new TorchCourier());
        Permanent target = addCreatureReady(player1, new DouserOfLights());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courier);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Granted haste wears off at end of turn")
    void grantedHasteWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TorchCourier());
        Permanent target = addCreatureReady(player1, new DouserOfLights());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Torch Courier itself")
    void cannotTargetItself() {
        Permanent courier = addCreatureReady(player1, new TorchCourier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, courier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeHasteResolves() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TorchCourier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DouserOfLights());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(courier);
        harness.assertInGraveyard(player1, "Torch Courier");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        declareAttackers(List.of(0));
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canGrantHasteToOpponentsCreature() {
        harness.addToBattlefield(player1, new TorchCourier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DouserOfLights());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        harness.assertInGraveyard(player1, "Torch Courier");
    }

    @Test
    void canActivateWhileTapped() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TorchCourier());
        courier.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DouserOfLights());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Torch Courier");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void courierCanAttackTheTurnItEnters() {
        Permanent courier = harness.addToBattlefieldAndReturn(player1, new TorchCourier());

        declareAttackers(List.of(0));

        assertThat(courier.isTapped()).isTrue();
    }
}
