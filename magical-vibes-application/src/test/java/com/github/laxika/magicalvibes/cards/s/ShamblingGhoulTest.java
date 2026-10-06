package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingGhoul.class})
class ShamblingGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Shambling Ghoul enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ShamblingGhoul(), "{1}{B}");
        harness.passBothPriorities();

        Permanent ghoul = findPermanent(player1, "Shambling Ghoul");
        assertThat(ghoul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shambling Ghoul untaps normally during its controller's untap step")
    void untapsNormally() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ShamblingGhoul(), "{1}{B}");
        harness.passBothPriorities();

        Permanent ghoul = findPermanent(player1, "Shambling Ghoul");
        assertThat(ghoul.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(ghoul.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(ghoul.isTapped()).isFalse();
    }
}
