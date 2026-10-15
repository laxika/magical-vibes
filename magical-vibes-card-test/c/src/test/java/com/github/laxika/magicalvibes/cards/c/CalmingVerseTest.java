package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuraFracture;
import com.github.laxika.magicalvibes.cards.b.BrutalSuppression;
import com.github.laxika.magicalvibes.cards.g.GlitteringLion;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CalmingVerse.class, AuraFracture.class, BrutalSuppression.class, GlitteringLion.class, RhysticCave.class})
class CalmingVerseTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys opposing enchantments and own enchantments with an untapped land")
    void destroysOpposingAndOwnEnchantmentsWithUntappedLand() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new GlitteringLion());
        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
        harness.assertOnBattlefield(player1, "Glittering Lion");
    }

    @Test
    @DisplayName("Destroys opposing enchantments but keeps own enchantments without an untapped land")
    void keepsOwnEnchantmentsWithoutUntappedLand() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();
        castAndResolve();

        harness.assertOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
    }

    @Test
    @DisplayName("Does not use an opponent's untapped land for the conditional destruction")
    void opposingUntappedLandDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        harness.addToBattlefield(player2, new RhysticCave());
        castAndResolve();

        harness.assertOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
        harness.assertOnBattlefield(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("Keeps own enchantments when the controller has no lands")
    void keepsOwnEnchantmentsWithNoLands() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        castAndResolve();

        harness.assertOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
    }

    @Test
    @DisplayName("Destroys own enchantments even when there are no opposing enchantments")
    void destroysOwnEnchantmentsWithoutOpposingEnchantments() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player1, new RhysticCave());
        castAndResolve();

        harness.assertNotOnBattlefield(player1, "Aura Fracture");
        harness.assertOnBattlefield(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("Keeps own enchantments if the last untapped land becomes tapped before resolution")
    void checksTappedLandAtResolution() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.castFromHand(player1, new CalmingVerse(), "{3}{G}");
        land.tap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
    }

    @Test
    @DisplayName("Destroys own enchantments if a land becomes untapped before resolution")
    void checksUntappedLandAtResolution() {
        harness.addToBattlefield(player1, new AuraFracture());
        harness.addToBattlefield(player2, new BrutalSuppression());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        land.tap();
        harness.castFromHand(player1, new CalmingVerse(), "{3}{G}");
        land.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aura Fracture");
        harness.assertNotOnBattlefield(player2, "Brutal Suppression");
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new CalmingVerse(), "{3}{G}");
        harness.passBothPriorities();
    }
}
