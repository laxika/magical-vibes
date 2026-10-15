package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildernessReclamation.class, Forest.class, GrizzlyBears.class})
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
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(secondForest.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new WildernessReclamation());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap an opponent's lands")
    void leavesOpponentLandsTapped() {
        harness.addToBattlefield(player1, new WildernessReclamation());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        ownForest.tap();
        opponentForest.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(ownForest.isTapped()).isFalse();
        assertThat(opponentForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps lands tapped or added after the ability triggers")
    void evaluatesLandsAtResolution() {
        harness.addToBattlefield(player1, new WildernessReclamation());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        forest.tap();
        Permanent newForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        newForest.tap();
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(newForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each copy triggers independently and can untap lands again")
    void multipleCopiesUntapSeparately() {
        harness.addToBattlefield(player1, new WildernessReclamation());
        harness.addToBattlefield(player1, new WildernessReclamation());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(2);
        assertThat(forest.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(forest.isTapped()).isFalse();

        forest.tap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(forest.isTapped()).isFalse();
    }
}
