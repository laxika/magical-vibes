package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BarkOfDoran;
import com.github.laxika.magicalvibes.cards.b.BreOfClanStoutarm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({Luminollusk.class, AirElemental.class, BarkOfDoran.class, BreOfClanStoutarm.class, Forest.class, GrizzlyBears.class, RagingGoblin.class})
class LuminolluskTest extends BaseCardTest {

    @Test
    void gainsOneLifeWhenItIsTheOnlyColorAmongControlledPermanents() {
        castLuminollusk();

        harness.assertLife(player1, 21);
    }

    @Test
    void gainsLifeForEachDistinctColorAmongControlledPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new RagingGoblin());

        castLuminollusk();

        harness.assertLife(player1, 23);
    }

    @Test
    void ignoresColorsAmongOpponentsPermanents() {
        harness.addToBattlefield(player2, new AirElemental());

        castLuminollusk();

        harness.assertLife(player1, 21);
    }

    @Test
    void countsRepeatedColorsOnlyOnceAndIgnoresColorlessLands() {
        harness.addToBattlefield(player1, new Luminollusk());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        castLuminollusk();

        harness.assertLife(player1, 21);
    }

    @Test
    void countsColorsAddedAfterTheTriggerWasCreated() {
        harness.castFromHand(player1, new Luminollusk(), "{3}{G}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.addToBattlefield(player1, new AirElemental());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggerStillResolvesWhenLuminolluskHasLeftTheBattlefield() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.castFromHand(player1, new Luminollusk(), "{3}{G}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof Luminollusk);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void gainsNoLifeWhenNoColoredPermanentsRemainAtResolution() {
        harness.castFromHand(player1, new Luminollusk(), "{3}{G}");
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.addToBattlefield(player1, new Forest());

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    void countsBothColorsOfAMulticoloredPermanent() {
        harness.addToBattlefield(player1, new BreOfClanStoutarm());

        castLuminollusk();

        harness.assertLife(player1, 23);
    }

    @Test
    void countsColorsOfNoncreaturePermanents() {
        harness.addToBattlefield(player1, new BarkOfDoran());

        castLuminollusk();

        harness.assertLife(player1, 22);
    }

    private void castLuminollusk() {
        harness.castFromHand(player1, new Luminollusk(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
