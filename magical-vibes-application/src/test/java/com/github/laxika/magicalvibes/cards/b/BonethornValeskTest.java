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
}
