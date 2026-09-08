package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FledglingGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Fledgling Griffin flying until end of turn")
    void landfallGrantsFlying() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new FledglingGriffin());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Landfall flying wears off at end of turn")
    void landfallFlyingWearsOff() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new FledglingGriffin());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's land does not trigger Fledgling Griffin")
    void opponentLandDoesNotTrigger() {
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new FledglingGriffin());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, griffin, Keyword.FLYING)).isFalse();
    }
}
