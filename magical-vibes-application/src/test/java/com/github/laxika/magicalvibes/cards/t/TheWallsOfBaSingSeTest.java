package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "The Walls of Ba Sing Se");
    }
}
