package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OneWithDeath.class, PlatinumAngel.class})
class OneWithDeathTest extends BaseCardTest {

    @Test
    @DisplayName("The spell's controller loses the game when it resolves")
    void controllerLosesTheGame() {
        harness.setHand(player1, List.of(new OneWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting the spell does not cause a loss before resolution")
    void lossWaitsForResolution() {
        harness.setHand(player1, List.of(new OneWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The opponent loses when they control the resolving spell")
    void opponentControllerLosesTheGame() {
        harness.setHand(player2, List.of(new OneWithDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Platinum Angel prevents the controller from losing")
    void cannotLoseEffectPreventsLoss() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setHand(player1, List.of(new OneWithDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "One with Death");
    }
}
