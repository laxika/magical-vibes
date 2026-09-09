package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakindiSliderunner.class, Forest.class})
class MakindiSliderunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Makindi Sliderunner +1/+1 until end of turn")
    void landfallBoostsMakindiSliderunner() {
        Permanent sliderunner = harness.addToBattlefieldAndReturn(player1, new MakindiSliderunner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(sliderunner.getEffectivePower()).isEqualTo(3);
        assertThat(sliderunner.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Makindi Sliderunner")
    void opponentLandDoesNotTrigger() {
        Permanent sliderunner = harness.addToBattlefieldAndReturn(player1, new MakindiSliderunner());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(sliderunner.getEffectivePower()).isEqualTo(2);
        assertThat(sliderunner.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall boost wears off at end of turn")
    void landfallBoostWearsOff() {
        Permanent sliderunner = harness.addToBattlefieldAndReturn(player1, new MakindiSliderunner());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(sliderunner.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sliderunner.getEffectivePower()).isEqualTo(2);
        assertThat(sliderunner.getEffectiveToughness()).isEqualTo(1);
    }
}
