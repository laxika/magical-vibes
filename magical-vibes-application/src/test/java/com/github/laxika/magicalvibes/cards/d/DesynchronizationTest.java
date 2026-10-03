package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TheAesirEscapeValhalla;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Desynchronization.class, FountainOfYouth.class, GrizzlyBears.class,
        IsamaruHoundOfKonda.class, Island.class, GloriousAnthem.class, TheAesirEscapeValhalla.class})
class DesynchronizationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns each nonland nonhistoric permanent to its owner's hand")
    void returnsEachNonlandNonhistoricPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.castFromHand(player1, new Desynchronization(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Returns ordinary enchantments while preserving nonlegendary Sagas")
    void returnsEnchantmentsButPreservesSagas() {
        harness.addToBattlefield(player1, new TheAesirEscapeValhalla());
        harness.addToBattlefield(player2, new TheAesirEscapeValhalla());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());

        harness.castFromHand(player1, new Desynchronization(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Aesir Escape Valhalla");
        harness.assertOnBattlefield(player2, "The Aesir Escape Valhalla");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertInHand(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Returns a stolen permanent to its owner's hand")
    void returnsStolenPermanentToOwner() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        harness.castFromHand(player1, new Desynchronization(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
