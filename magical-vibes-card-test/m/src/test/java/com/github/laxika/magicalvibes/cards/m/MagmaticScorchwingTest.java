package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({MagmaticScorchwing.class, EvolvingWilds.class, Forest.class, GrizzlyBears.class})
class MagmaticScorchwingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage when the library has no nonbasic land cards")
    void dealsDamageWithNoNonbasicLandsInLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        castScorchwing();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the library contains a nonbasic land card")
    void doesNotTriggerWithNonbasicLandInLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        castScorchwing();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 3 damage to a player with an empty library")
    void damagesPlayerWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castScorchwing();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Nonland cards and the opponent's nonbasic lands do not prevent damage")
    void ignoresNonlandCardsAndOpponentsLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new EvolvingWilds()));
        castScorchwing();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Does no damage if a nonbasic land is in the library when the trigger resolves")
    void rechecksLibraryAtResolution() {
        harness.setLibrary(player1, List.of(new Forest()));
        castScorchwing();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing a nonbasic land after entry does not create a trigger")
    void doesNotTriggerRetroactively() {
        harness.setLibrary(player1, List.of(new EvolvingWilds()));
        castScorchwing();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    private void castScorchwing() {
        harness.castFromHand(player1, new MagmaticScorchwing(), "{3}{R}{R}");
        harness.passBothPriorities();
    }
}
