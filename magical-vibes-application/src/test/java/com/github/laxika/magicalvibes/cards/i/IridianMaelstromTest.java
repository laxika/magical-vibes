package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.f.FusionElemental;
import com.github.laxika.magicalvibes.cards.j.JaredCarthalion;
import com.github.laxika.magicalvibes.cards.j.JodahTheUnifier;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

@CardUsed({IridianMaelstrom.class, GrizzlyBears.class, JodahTheUnifier.class, Plains.class,
        BalefulStrix.class, FusionElemental.class, JaredCarthalion.class, SolemnSimulacrum.class})
class IridianMaelstromTest extends BaseCardTest {

    @Test
    void destroysCreaturesThatAreNotAllColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new JodahTheUnifier());
        harness.addToBattlefield(player1, new Plains());

        harness.castFromHand(player1, new IridianMaelstrom(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Jodah, the Unifier");
        harness.assertOnBattlefield(player1, "Plains");
    }

    @Test
    void destroysColorlessAndTwoColorCreaturesButSparesAllColorCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new SolemnSimulacrum());
        harness.addToBattlefield(player2, new BalefulStrix());
        harness.addToBattlefield(player1, new FusionElemental());
        harness.addToBattlefield(player2, new FusionElemental());
        harness.addToBattlefield(player2, new JaredCarthalion());

        harness.castFromHand(player1, new IridianMaelstrom(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Solemn Simulacrum");
        harness.assertInGraveyard(player1, "Solemn Simulacrum");
        harness.assertNotOnBattlefield(player2, "Baleful Strix");
        harness.assertInGraveyard(player2, "Baleful Strix");
        harness.assertOnBattlefield(player1, "Fusion Elemental");
        harness.assertOnBattlefield(player2, "Fusion Elemental");
        harness.assertOnBattlefield(player2, "Jared Carthalion");
    }

    @Test
    void resolvesWithoutAnyCreatures() {
        harness.castFromHand(player1, new IridianMaelstrom(), "{W}{U}{B}{R}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Iridian Maelstrom");
    }
}
