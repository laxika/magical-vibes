package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({WanderingStream.class, Forest.class, Island.class, Swamp.class, Plains.class, Mountain.class, CoastalTower.class})
class WanderingStreamTest extends BaseCardTest {

    @Test
    void gainsTwoLifeForEachDistinctBasicLandTypeYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new WanderingStream()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore + 4);
    }

    @Test
    void gainsNoLifeWithoutBasicLandTypesYouControl() {
        harness.setHand(player1, List.of(new WanderingStream()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    void countsBasicLandTypesWhenTheSpellResolves() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new WanderingStream()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 6);
    }

    @Test
    void gainsTenLifeWithAllFiveBasicLandTypes() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new WanderingStream()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore + 10);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    void manaColorsOfNonbasicLandsDoNotCountAsBasicLandTypes() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CoastalTower());
        harness.setHand(player1, List.of(new WanderingStream()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore + 2);
    }
}
