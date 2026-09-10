package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.e.Earthcraft;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Tranquility.class, Earthcraft.class, Propaganda.class, MoggFanatic.class, GiantStrength.class})
class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new Earthcraft());
        harness.addToBattlefield(player2, new Propaganda());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Earthcraft");
        harness.assertNotOnBattlefield(player2, "Propaganda");
        harness.assertInGraveyard(player1, "Earthcraft");
        harness.assertInGraveyard(player2, "Propaganda");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        Permanent fanatic = addCreatureReady(player1, new MoggFanatic());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new GiantStrength());
        auraPerm.setAttachedTo(fanatic.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Strength");
        harness.assertOnBattlefield(player1, "Mogg Fanatic");
        harness.assertInGraveyard(player1, "Giant Strength");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new MoggFanatic());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mogg Fanatic");
    }
}
