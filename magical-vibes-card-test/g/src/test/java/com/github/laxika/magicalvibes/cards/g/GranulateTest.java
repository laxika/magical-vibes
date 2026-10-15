package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.e.EonHub;
import com.github.laxika.magicalvibes.cards.f.FurnaceWhelp;
import com.github.laxika.magicalvibes.cards.m.MyrQuadropod;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Granulate.class, EonHub.class, FurnaceWhelp.class, MyrQuadropod.class,
        MyrServitor.class, SeatOfTheSynod.class, ParadiseMantle.class, DarksteelIngot.class})
class GranulateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys each nonland artifact with mana value 4 or less")
    void destroysMatchingArtifactsAcrossBothBattlefields() {
        harness.addToBattlefield(player1, new MyrServitor());
        harness.addToBattlefield(player1, new MyrQuadropod());
        harness.addToBattlefield(player1, new EonHub());
        harness.addToBattlefield(player1, new FurnaceWhelp());
        harness.addToBattlefield(player1, new SeatOfTheSynod());
        harness.addToBattlefield(player2, new MyrServitor());

        harness.castFromHand(player1, new Granulate(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Myr Servitor");
        harness.assertNotOnBattlefield(player2, "Myr Servitor");
        harness.assertNotOnBattlefield(player1, "Myr Quadropod");
        harness.assertOnBattlefield(player1, "Eon Hub");
        harness.assertOnBattlefield(player1, "Furnace Whelp");
        harness.assertOnBattlefield(player1, "Seat of the Synod");
    }

    @Test
    @DisplayName("Destroys zero-cost noncreature artifacts but preserves indestructible artifacts")
    void destroysZeroCostArtifactsAndRespectsIndestructible() {
        harness.addToBattlefield(player1, new ParadiseMantle());
        harness.addToBattlefield(player2, new ParadiseMantle());
        harness.addToBattlefield(player2, new DarksteelIngot());

        harness.castFromHand(player1, new Granulate(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Paradise Mantle");
        harness.assertInGraveyard(player2, "Paradise Mantle");
        harness.assertNotOnBattlefield(player1, "Paradise Mantle");
        harness.assertNotOnBattlefield(player2, "Paradise Mantle");
        harness.assertOnBattlefield(player2, "Darksteel Ingot");
        harness.assertNotInGraveyard(player2, "Darksteel Ingot");
    }

    @Test
    @DisplayName("Resolves without eligible artifacts")
    void resolvesWithoutEligibleArtifacts() {
        harness.addToBattlefield(player1, new EonHub());
        harness.addToBattlefield(player2, new FurnaceWhelp());

        harness.castFromHand(player1, new Granulate(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eon Hub");
        harness.assertOnBattlefield(player2, "Furnace Whelp");
        harness.assertInGraveyard(player1, "Granulate");
    }
}
