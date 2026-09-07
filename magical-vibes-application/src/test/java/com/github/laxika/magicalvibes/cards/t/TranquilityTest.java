package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.b.BadMoon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pestilence;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Tranquility.class, BadMoon.class, Pestilence.class, GrizzlyBears.class, HolyStrength.class})
class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new BadMoon());
        harness.addToBattlefield(player2, new Pestilence());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bad Moon");
        harness.assertNotOnBattlefield(player2, "Pestilence");
        harness.assertInGraveyard(player1, "Bad Moon");
        harness.assertInGraveyard(player2, "Pestilence");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        auraPerm.setAttachedTo(bears.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
