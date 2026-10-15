package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderseaInvader.class})
class UnderseaInvaderTest extends BaseCardTest {

    @Test
    @DisplayName("Undersea Invader enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new UnderseaInvader(), "{4}{U}{U}");
        harness.passBothPriorities();

        Permanent invader = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(invader.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's end step, but it still enters tapped")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.castFromHand(player1, new UnderseaInvader(), "{4}{U}{U}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering tapped does not prevent untapping during the controller's untap step")
    void untapsNormally() {
        harness.castFromHand(player1, new UnderseaInvader(), "{4}{U}{U}");
        harness.passBothPriorities();
        Permanent invader = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(invader.isTapped()).isTrue();

        harness.performUntapStep(player1);

        assertThat(invader.isTapped()).isFalse();
    }
}
