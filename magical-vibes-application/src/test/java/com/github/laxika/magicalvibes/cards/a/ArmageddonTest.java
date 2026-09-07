package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.Taiga;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Armageddon.class, Forest.class, Mountain.class, Island.class, GrizzlyBears.class, Taiga.class})
class ArmageddonTest extends BaseCardTest {

    @Test
    @CardUsed({Forest.class, Mountain.class, Island.class})
    @DisplayName("Destroys all lands controlled by both players")
    void destroysAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @CardUsed(Taiga.class)
    @DisplayName("Destroys nonbasic lands")
    void destroysNonbasicLands() {
        harness.addToBattlefield(player1, new Taiga());
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Taiga");
        harness.assertInGraveyard(player1, "Taiga");
    }

    @Test
    @CardUsed(GrizzlyBears.class)
    @DisplayName("Does not destroy non-land permanents")
    void doesNotDestroyNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
