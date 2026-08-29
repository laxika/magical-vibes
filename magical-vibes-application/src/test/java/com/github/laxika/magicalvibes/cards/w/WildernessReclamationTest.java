package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildernessReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all lands you control at your end step")
    void untapsControlledLandsAtControllerEndStep() {
        harness.addToBattlefield(player1, new WildernessReclamation());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        forest.tap();
        secondForest.tap();
        bear.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(secondForest.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
    }
}
