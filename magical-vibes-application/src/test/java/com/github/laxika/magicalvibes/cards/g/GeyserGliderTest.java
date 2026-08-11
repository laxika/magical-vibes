package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GeyserGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall gives Geyser Glider flying until end of turn")
    void landfallGrantsFlying() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new GeyserGlider());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Landfall flying wears off at end of turn")
    void landfallFlyingWearsOff() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new GeyserGlider());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's landfall does not grant flying")
    void opponentLandDoesNotTrigger() {
        Permanent glider = harness.addToBattlefieldAndReturn(player1, new GeyserGlider());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glider, Keyword.FLYING)).isFalse();
    }
}
