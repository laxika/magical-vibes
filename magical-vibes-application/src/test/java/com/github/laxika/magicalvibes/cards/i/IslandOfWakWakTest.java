package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IslandOfWakWak.class, SuntailHawk.class, GrizzlyBears.class})
class IslandOfWakWakTest extends BaseCardTest {

    @Test
    @DisplayName("Sets a flying creature's base power to zero until end of turn")
    void setsFlyingCreatureBasePowerToZero() {
        Permanent land = addLand();
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        activate(hawk);

        assertThat(gqs.getEffectivePower(gd, hawk)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("The base power set wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent land = addLand();
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        activate(hawk);
        assertThat(gqs.getEffectivePower(gd, hawk)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetCreatureWithoutFlying() {
        Permanent land = addLand();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(land.isTapped()).isFalse();
    }

    private Permanent addLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new IslandOfWakWak());
    }

    private void activate(Permanent target) {
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
