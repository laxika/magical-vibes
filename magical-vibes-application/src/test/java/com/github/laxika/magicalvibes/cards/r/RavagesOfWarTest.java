package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavagesOfWar.class, Forest.class, ForestBear.class, Island.class, Mountain.class})
class RavagesOfWarTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all lands controlled by both players")
    void destroysAllLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        harness.castFromHand(player1, new RavagesOfWar(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Island");
    }

    @Test
    @DisplayName("Does not destroy non-land permanents")
    void doesNotDestroyNonLands() {
        harness.addToBattlefield(player1, new ForestBear());
        harness.addToBattlefield(player2, new ForestBear());
        harness.castFromHand(player1, new RavagesOfWar(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest Bear");
        harness.assertOnBattlefield(player2, "Forest Bear");
    }

    @Test
    @DisplayName("Resolves normally when neither player controls any permanents")
    void resolvesWithEmptyBattlefield() {
        harness.castFromHand(player1, new RavagesOfWar(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ravages of War");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Destroys tapped lands and lands entering before resolution")
    void determinesLandsAtResolution() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.castFromHand(player1, new RavagesOfWar(), "{3}{W}");

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        harness.addToBattlefield(player2, new Island());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Island");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Island");
        harness.assertInGraveyard(player1, "Ravages of War");
    }
}
