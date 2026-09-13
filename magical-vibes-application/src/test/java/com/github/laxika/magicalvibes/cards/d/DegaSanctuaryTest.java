package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BloodfireDwarf;
import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.cards.m.MournfulZombie;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({DegaSanctuary.class, BloodfireDwarf.class, Cromat.class, MournfulZombie.class})
class DegaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life when you control a black permanent only")
    void gainsTwoLifeWithBlackPermanentOnly() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new MournfulZombie());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains 2 life when you control a red permanent only")
    void gainsTwoLifeWithRedPermanentOnly() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new BloodfireDwarf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains 4 life when you control both a black and a red permanent")
    void gainsFourLifeWithBlackAndRedPermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new MournfulZombie());
        harness.addToBattlefield(player1, new BloodfireDwarf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Gains 4 life when one permanent is both black and red")
    void gainsFourLifeWithOneBlackAndRedPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new Cromat());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Does not gain life without a black or red permanent")
    void doesNotGainLifeWithoutBlackOrRedPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life for an opponent's black or red permanent")
    void ignoresOpponentsColoredPermanent() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player2, new BloodfireDwarf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers only during the controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        harness.addToBattlefield(player1, new BloodfireDwarf());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing if the qualifying permanent leaves before resolution")
    void doesNothingIfQualifyingPermanentLeavesBeforeResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DegaSanctuary());
        Permanent redPermanent = harness.addToBattlefieldAndReturn(player1, new BloodfireDwarf());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(redPermanent);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }
}
