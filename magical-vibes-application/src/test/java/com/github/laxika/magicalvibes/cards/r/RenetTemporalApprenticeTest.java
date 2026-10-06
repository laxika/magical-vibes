package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.util.List.of;

@CardUsed({RenetTemporalApprentice.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class, Unsummon.class})
@DisplayName("Renet, Temporal Apprentice")
class RenetTemporalApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns other nonland permanents that entered this turn")
    void returnsOtherNonlandPermanentsThatEnteredThisTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.enterBattlefieldAndReturn(player2, new Forest());

        castRenet();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Renet, Temporal Apprentice");
    }

    @Test
    @DisplayName("Returns new permanents controlled by both players")
    void returnsNewPermanentsControlledByBothPlayers() {
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());

        castRenet();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Renet, Temporal Apprentice");
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn using flash")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new RenetTemporalApprentice(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Renet, Temporal Apprentice");
    }

    @Test
    @DisplayName("A pending trigger returns Renet if she leaves and reenters")
    void pendingTriggerReturnsReenteredRenet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        RenetTemporalApprentice renet = new RenetTemporalApprentice();
        harness.castFromHand(player1, renet, "{3}{U}{U}");
        harness.passBothPriorities();
        var originalPermanent = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.setHand(player1, of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, originalPermanent.getId());
        harness.assertInHand(player1, "Renet, Temporal Apprentice");

        harness.castFromHand(player1, renet, "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Renet, Temporal Apprentice");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Renet, Temporal Apprentice");
        harness.assertInHand(player1, "Renet, Temporal Apprentice");
    }

    private void castRenet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RenetTemporalApprentice(), "{3}{U}{U}");
    }
}
