package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DreamChisel;
import com.github.laxika.magicalvibes.cards.e.EnchantresssPresence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({AkromasVengeance.class, DreamChisel.class, EnchantresssPresence.class, Forest.class, GlorySeeker.class})
class AkromasVengeanceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all artifacts, creatures, and enchantments")
    void destroysArtifactsCreaturesAndEnchantments() {
        harness.addToBattlefield(player1, new DreamChisel());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new EnchantresssPresence());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new AkromasVengeance(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dream Chisel");
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
        harness.assertNotOnBattlefield(player2, "Enchantress's Presence");
        harness.assertNotOnBattlefield(player2, "Glory Seeker");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Regeneration can save a destroyed permanent")
    void regenerationCanSaveAPermanent() {
        Permanent glorySeeker = harness.addToBattlefieldAndReturn(player1, new GlorySeeker());
        glorySeeker.setRegenerationShield(1);

        harness.castFromHand(player1, new AkromasVengeance(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glory Seeker");
        harness.assertNotInGraveyard(player1, "Glory Seeker");
    }

    @Test
    @DisplayName("Cycling {3} discards Akroma's Vengeance and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new AkromasVengeance()));
        harness.setLibrary(player1, List.of(new GlorySeeker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Akroma's Vengeance");
        harness.assertInHand(player1, "Glory Seeker");
    }
}
