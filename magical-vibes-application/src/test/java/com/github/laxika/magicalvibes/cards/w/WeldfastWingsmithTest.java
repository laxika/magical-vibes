package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeldfastWingsmithTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact you control entering gives Weldfast Wingsmith flying until end of turn")
    void allyArtifactEnterGrantsFlying() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying granted by the artifact trigger wears off at end of turn")
    void flyingWearsOffAtCleanup() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.setHand(player1, List.of(new GlazeFiend()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An artifact an opponent controls entering does not grant flying")
    void opponentArtifactEnterDoesNotTrigger() {
        Permanent wingsmith = harness.addToBattlefieldAndReturn(player1, new WeldfastWingsmith());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlazeFiend()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wingsmith, Keyword.FLYING)).isFalse();
    }
}
