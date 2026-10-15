package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWallsOfBaSingSe.class, Forest.class, GrizzlyBears.class, WrathOfGod.class})
class TheWallsOfBaSingSeTest extends BaseCardTest {

    @Test
    @DisplayName("Other permanents you control have indestructible")
    void grantsIndestructibleToOtherOwnPermanents() {
        harness.addToBattlefield(player1, new TheWallsOfBaSingSe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        Permanent walls = findPermanent(player1, "The Walls of Ba Sing Se");
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        Permanent land = findPermanent(player1, "Forest");

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, walls, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant indestructible to an opponent's permanents")
    void doesNotAffectOpponentsPermanents() {
        harness.addToBattlefield(player1, new TheWallsOfBaSingSe());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());

        Permanent creature = findPermanent(player2, "Grizzly Bears");
        Permanent land = findPermanent(player2, "Forest");

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Protected permanents survive destruction while the source does not protect itself")
    void protectedPermanentsSurviveWrathOfGod() {
        harness.addToBattlefield(player1, new TheWallsOfBaSingSe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "The Walls of Ba Sing Se");
    }

    @Test
    @DisplayName("Protection ends when the Walls leaves the battlefield")
    void protectionEndsWhenWallsIsDestroyed() {
        harness.addToBattlefield(player1, new TheWallsOfBaSingSe());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "The Walls of Ba Sing Se");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Forest"), Keyword.INDESTRUCTIBLE)).isFalse();

        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
