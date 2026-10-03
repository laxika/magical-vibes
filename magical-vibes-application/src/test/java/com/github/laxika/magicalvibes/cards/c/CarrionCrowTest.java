package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CarrionCrow.class})
class CarrionCrowTest extends BaseCardTest {

    @Test
    @DisplayName("Carrion Crow enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CarrionCrow(), "{2}{B}");
        harness.passBothPriorities();

        Permanent crow = findPermanent(player1, "Carrion Crow");
        assertThat(crow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Carrion Crow enters tapped even when it is not cast")
    void entersTappedWithoutBeingCast() {
        Permanent crow = harness.enterBattlefieldAndReturn(player2, new CarrionCrow());

        assertThat(crow.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Carrion Crow untaps normally during its controller's untap step")
    void untapsNormally() {
        Permanent crow = harness.enterBattlefieldAndReturn(player1, new CarrionCrow());
        assertThat(crow.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(crow.isTapped()).isFalse();
    }
}
