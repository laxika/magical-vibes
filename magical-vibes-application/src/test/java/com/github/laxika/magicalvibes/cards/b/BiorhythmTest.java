package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Biorhythm.class, GrizzlyBears.class, Forest.class})
class BiorhythmTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's life total becomes the number of creatures they control")
    void setsLifeToCreatureCount() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 2);
        harness.assertLife(player2, 1);
    }

    @Test
    @DisplayName("A player controlling no creatures has their life total set to 0")
    void setsLifeToZeroWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }

    @Test
    @DisplayName("Does not count noncreature permanents")
    void ignoresNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 1);
        harness.assertLife(player2, 0);
    }
}
