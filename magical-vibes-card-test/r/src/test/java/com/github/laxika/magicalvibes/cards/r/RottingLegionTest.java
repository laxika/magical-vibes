package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RottingLegion.class})
class RottingLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Rotting Legion enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RottingLegion(), "{4}{B}");
        harness.passBothPriorities();

        Permanent legion = findPermanent(player1, "Rotting Legion");
        assertThat(legion.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rotting Legion untaps during its controller's untap step")
    void untapsNormallyAfterEnteringTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RottingLegion(), "{4}{B}");
        harness.passBothPriorities();

        Permanent legion = findPermanent(player1, "Rotting Legion");
        assertThat(legion.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(legion.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(legion.isTapped()).isFalse();
    }
}
