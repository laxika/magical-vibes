package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({PeachGardenOath.class, GlorySeeker.class, Forest.class})
class PeachGardenOathTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each creature you control")
    void gainsTwoLifePerControlledCreature() {
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GlorySeeker());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Does not count opponent's creatures")
    void doesNotCountOpponentCreatures() {
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player2, new GlorySeeker());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count non-creature permanents")
    void doesNotCountNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains no life when controlling no creatures")
    void gainsNoLifeWithNoCreatures() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counts creatures when the spell resolves")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.addToBattlefield(player1, new GlorySeeker());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counts tapped creatures but excludes lands and opposing creatures")
    void countsOnlyControlledCreaturesOnMixedBattlefield() {
        harness.addToBattlefieldAndReturn(player1, new GlorySeeker()).setTapped(true);
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);

        harness.castFromHand(player1, new PeachGardenOath(), "{W}");
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Does not count a creature that left before resolution")
    void excludesCreatureThatLeftBeforeResolution() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new PeachGardenOath(), "{W}");

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
