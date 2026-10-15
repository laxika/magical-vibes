package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiranhaFly.class})
class PiranhaFlyTest extends BaseCardTest {

    @Test
    @DisplayName("Piranha Fly enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new PiranhaFly(), "{1}{U}");
        harness.passBothPriorities();

        Permanent fly = findPermanent(player1, "Piranha Fly");
        assertThat(fly.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Piranha Fly enters tapped even when it is not cast")
    void entersTappedWithoutBeingCast() {
        Permanent fly = harness.enterBattlefieldAndReturn(player2, new PiranhaFly());

        assertThat(fly.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Piranha Fly");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering tapped does not prevent normal untapping")
    void untapsDuringControllersUntapStep() {
        Permanent fly = harness.enterBattlefieldAndReturn(player1, new PiranhaFly());
        assertThat(fly.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(fly.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(fly.isTapped()).isFalse();
    }
}
