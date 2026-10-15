package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnhallowedPhalanx.class})
class UnhallowedPhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Unhallowed Phalanx enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new UnhallowedPhalanx(), "{4}{B}");
        harness.passBothPriorities();

        Permanent phalanx = findPermanent(player1, "Unhallowed Phalanx");
        assertThat(phalanx.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unhallowed Phalanx untaps normally during its controller's untap step")
    void untapsNormallyAfterEnteringTapped() {
        harness.castFromHand(player1, new UnhallowedPhalanx(), "{4}{B}");
        harness.passBothPriorities();

        Permanent phalanx = findPermanent(player1, "Unhallowed Phalanx");
        assertThat(phalanx.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(phalanx.isTapped()).isFalse();
    }
}
