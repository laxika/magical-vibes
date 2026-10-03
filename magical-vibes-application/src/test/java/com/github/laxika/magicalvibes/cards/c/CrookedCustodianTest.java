package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrookedCustodian.class})
class CrookedCustodianTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new CrookedCustodian(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crooked Custodian").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when put onto the battlefield without being cast")
    void entersTappedWithoutBeingCast() {
        Permanent custodian = harness.enterBattlefieldAndReturn(player2, new CrookedCustodian());

        assertThat(custodian.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untaps normally during its controller's untap step")
    void untapsNormallyDuringControllersUntapStep() {
        Permanent custodian = harness.enterBattlefieldAndReturn(player1, new CrookedCustodian());
        assertThat(custodian.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(custodian.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(custodian.isTapped()).isFalse();
    }
}
