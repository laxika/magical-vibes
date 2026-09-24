package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.ArmadilloCloak;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Tranquility.class, DuelingGrounds.class, ArmadilloCloak.class, LlanowarVanguard.class, Forest.class})
class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player2, new DuelingGrounds());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dueling Grounds");
        harness.assertNotOnBattlefield(player2, "Dueling Grounds");
        harness.assertInGraveyard(player1, "Dueling Grounds");
        harness.assertInGraveyard(player2, "Dueling Grounds");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArmadilloCloak());
        auraPerm.setAttachedTo(vanguard.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armadillo Cloak");
        harness.assertOnBattlefield(player1, "Llanowar Vanguard");
        harness.assertInGraveyard(player1, "Armadillo Cloak");
    }

    @Test
    @DisplayName("Does not destroy creatures or lands")
    void doesNotDestroyCreaturesOrLands() {
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Vanguard");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
