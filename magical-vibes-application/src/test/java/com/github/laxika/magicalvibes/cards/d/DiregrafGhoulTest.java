package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiregrafGhoul.class})
class DiregrafGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Diregraf Ghoul enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DiregrafGhoul(), "{B}");
        harness.passBothPriorities();

        Permanent ghoul = findPermanent(player1, "Diregraf Ghoul");
        assertThat(ghoul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Diregraf Ghoul enters tapped without being cast and creates no tap trigger")
    void entersTappedWithoutBeingCast() {
        Permanent ghoul = harness.enterBattlefieldAndReturn(player2, new DiregrafGhoul());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ghoul);
        assertThat(ghoul.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Diregraf Ghoul untaps normally during its controller's untap step")
    void untapsNormallyAfterEnteringTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DiregrafGhoul(), "{B}");
        harness.passBothPriorities();

        Permanent ghoul = findPermanent(player1, "Diregraf Ghoul");
        assertThat(ghoul.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(ghoul.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(ghoul.isTapped()).isFalse();
    }
}
