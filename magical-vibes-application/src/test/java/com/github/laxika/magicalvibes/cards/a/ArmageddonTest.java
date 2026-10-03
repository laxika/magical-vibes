package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Brushland;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Armageddon.class, Forest.class, Mountain.class, Island.class, GrizzlyBears.class,
        Brushland.class, DarksteelCitadel.class})
class ArmageddonTest extends BaseCardTest {

    @Test
    @CardUsed({Armageddon.class, Forest.class, Mountain.class, Island.class})
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
    @CardUsed({Armageddon.class, Brushland.class})
    @DisplayName("Destroys nonbasic lands")
    void destroysNonbasicLands() {
        harness.addToBattlefield(player1, new Brushland());
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Brushland");
        harness.assertInGraveyard(player1, "Brushland");
    }

    @Test
    @CardUsed({Armageddon.class, GrizzlyBears.class})
    @DisplayName("Does not destroy non-land permanents")
    void doesNotDestroyNonLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(Armageddon.class)
    @DisplayName("Resolves when there are no lands")
    void resolvesWithoutLands() {
        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Armageddon");
    }

    @Test
    @CardUsed({Armageddon.class, DarksteelCitadel.class, Forest.class})
    @DisplayName("Indestructible artifact lands survive while other lands are destroyed")
    void indestructibleLandSurvives() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new Forest());

        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertNotInGraveyard(player1, "Darksteel Citadel");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @CardUsed({Armageddon.class, Forest.class, Island.class})
    @DisplayName("Destroys tapped lands as well as untapped lands")
    void destroysTappedLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.tapPermanent(player1, 0);

        harness.castFromHand(player1, new Armageddon(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player2, "Island");
    }
}
