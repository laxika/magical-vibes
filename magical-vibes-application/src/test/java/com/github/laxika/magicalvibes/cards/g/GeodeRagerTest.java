package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeodeRager.class, Forest.class, GrizzlyBears.class})
class GeodeRagerTest extends BaseCardTest {

    @Test
    void landfallGoadsEachCreatureTargetPlayerControls() {
        harness.addToBattlefield(player1, new GeodeRager());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.isGoaded(gd, firstTarget)).isTrue();
        assertThat(gqs.isGoaded(gd, secondTarget)).isTrue();
        assertThat(gqs.isGoaded(gd, ownCreature)).isFalse();
    }

    @Test
    void creaturesEnteringAfterLandfallResolvesAreNotGoaded() {
        harness.addToBattlefield(player1, new GeodeRager());
        Permanent existingTarget = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent laterTarget = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.isGoaded(gd, existingTarget)).isTrue();
        assertThat(gqs.isGoaded(gd, laterTarget)).isFalse();
    }

    @Test
    void goadExpiresAtControllersNextTurn() {
        harness.addToBattlefield(player1, new GeodeRager());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, target)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }
}
