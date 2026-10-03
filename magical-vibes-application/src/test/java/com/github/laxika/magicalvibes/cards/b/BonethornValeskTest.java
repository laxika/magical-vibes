package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FyndhornElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonethornValesk.class, Forest.class, FyndhornElves.class})
class BonethornValeskTest extends BaseCardTest {

    @Test
    void dealsDamageWhenItTurnsFaceUp() {
        Permanent bonethorn = harness.addToBattlefieldAndReturn(player1, new BonethornValesk());
        bonethorn.setFaceDownAsCloaked();
        harness.setLife(player2, 20);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, bonethorn);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsDamageToTargetPlayerWhenAnyPermanentTurnsFaceUp() {
        harness.addToBattlefield(player1, new BonethornValesk());
        Permanent faceDownForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        faceDownForest.setFaceDownAsCloaked();
        harness.setLife(player2, 20);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDownForest);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsDamageToTargetCreatureWhenAnyPermanentTurnsFaceUp() {
        harness.addToBattlefield(player1, new BonethornValesk());
        Permanent faceDownForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        faceDownForest.setFaceDownAsCloaked();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FyndhornElves());

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, faceDownForest);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fyndhorn Elves");
    }

    @Test
    void canTargetItsOwnControllerWhenAnAlliedPermanentTurnsFaceUp() {
        harness.addToBattlefield(player1, new BonethornValesk());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setFaceDownAsCloaked();
        harness.setLife(player1, 20);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, forest);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void opponentControlsTheTriggerWhenTheirBonethornTurnsFaceUp() {
        Permanent bonethorn = harness.addToBattlefieldAndReturn(player2, new BonethornValesk());
        bonethorn.setFaceDownAsCloaked();
        harness.setLife(player1, 20);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, bonethorn);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void faceDownBonethornDoesNotTriggerWhenAnotherPermanentTurnsFaceUp() {
        Permanent bonethorn = harness.addToBattlefieldAndReturn(player1, new BonethornValesk());
        bonethorn.setFaceDownAsCloaked();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.setFaceDownAsCloaked();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, forest);

        assertThat(forest.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
