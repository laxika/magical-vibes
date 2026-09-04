package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MysticRemora;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Jokulhaups.class, BalduvianBears.class, ZuranOrb.class, MysticRemora.class, Forest.class})
class JokulhaupsTest extends BaseCardTest {

    @Test
    @DisplayName("Jokulhaups destroys all artifacts, creatures, and lands")
    void destroysArtifactsCreaturesAndLands() {
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Zuran Orb");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Jokulhaups does not destroy enchantments")
    void doesNotDestroyEnchantments() {
        harness.addToBattlefield(player1, new MysticRemora());

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mystic Remora");
    }

    @Test
    @DisplayName("Destroyed permanents can't be regenerated")
    void destroyedPermanentsCannotBeRegenerated() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        bears.setRegenerationShield(1);

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        // Regeneration shield does not save the creature from Jokulhaups.
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Indestructible permanents survive Jokulhaups")
    void indestructiblePermanentsSurvive() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.castFromHand(player1, new Jokulhaups(), "{4}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }
}
