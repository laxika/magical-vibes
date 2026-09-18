package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrenellatedWall.class, CreditVoucher.class})
class CrenellatedWallTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Crenellated Wall gives target creature +0/+4 until end of turn")
    void boostsTargetCreature() {
        Permanent wall = addCreatureReady(player1, new CrenellatedWall());
        Permanent target = addCreatureReady(player2, new CrenellatedWall());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
    }

    @Test
    @DisplayName("The toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CrenellatedWall());
        Permanent target = addCreatureReady(player2, new CrenellatedWall());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new CrenellatedWall());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CreditVoucher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate again while Crenellated Wall is tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new CrenellatedWall());
        Permanent target = addCreatureReady(player2, new CrenellatedWall());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }
}
