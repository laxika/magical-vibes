package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({WanderingStream.class, Forest.class, Island.class, Swamp.class})
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
}
