package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.e.EyeOfRamos;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.s.SpidersilkArmor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Tranquility.class, SpidersilkArmor.class, Arrest.class, FreshVolunteers.class,
        Forest.class, EyeOfRamos.class})
class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        harness.addToBattlefield(player2, new SpidersilkArmor());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spidersilk Armor");
        harness.assertNotOnBattlefield(player2, "Spidersilk Armor");
        harness.assertInGraveyard(player1, "Spidersilk Armor");
        harness.assertInGraveyard(player2, "Spidersilk Armor");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        Permanent volunteer = addCreatureReady(player1, new FreshVolunteers());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Arrest());
        auraPerm.setAttachedTo(volunteer.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Arrest");
        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Arrest");
    }

    @Test
    @DisplayName("Does not destroy creatures")
    void doesNotDestroyCreatures() {
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Does not destroy non-enchantment artifacts or lands")
    void doesNotDestroyNonEnchantmentArtifactsOrLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new EyeOfRamos());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Eye of Ramos");
    }
}
