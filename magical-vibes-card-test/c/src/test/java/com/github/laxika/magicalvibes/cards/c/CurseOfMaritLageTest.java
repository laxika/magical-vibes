package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfMaritLage.class, Island.class, Forest.class})
class CurseOfMaritLageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps every Island on every battlefield but leaves other lands alone")
    void entryTapsAllIslands() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new CurseOfMaritLage(), "{3}{R}{R}");
        resolveAllTriggers();

        assertThat(ownIsland.isTapped()).isTrue();
        assertThat(opponentIsland.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB taps Islands that enter before its triggered ability resolves")
    void etbTapsIslandsEnteringBeforeTriggerResolves() {
        harness.castFromHand(player1, new CurseOfMaritLage(), "{3}{R}{R}");
        harness.passBothPriorities(); // resolve the enchantment, leaving its ETB trigger on the stack

        Permanent lateIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent lateForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.passBothPriorities();

        assertThat(lateIsland.isTapped()).isTrue();
        assertThat(lateForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Islands stay tapped through their controller's untap step")
    void islandsDoNotUntap() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        island.tap();
        forest.tap();
        harness.addToBattlefield(player2, new CurseOfMaritLage());

        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects an opponent's Island during that player's untap step")
    void opponentIslandsDoNotUntap() {
        harness.addToBattlefield(player1, new CurseOfMaritLage());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        opponentIsland.tap();
        opponentForest.tap();

        advanceToUpkeep(player2);

        assertThat(opponentIsland.isTapped()).isTrue();
        assertThat(opponentForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The enter trigger still taps Islands after the enchantment leaves")
    void enterTriggerSurvivesSourceLeaving() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.castFromHand(player1, new CurseOfMaritLage(), "{3}{R}{R}");
        harness.passBothPriorities();

        Permanent curse = findPermanent(player1, "Curse of Marit Lage");
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();

        advanceToUpkeep(player2);

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Removing one Curse does not end another Curse's untap restriction")
    void remainingCurseStillPreventsUntapping() {
        Permanent firstCurse = harness.addToBattlefieldAndReturn(player1, new CurseOfMaritLage());
        harness.addToBattlefield(player2, new CurseOfMaritLage());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        gd.playerBattlefields.get(player1.getId()).remove(firstCurse);
        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Islands untap normally after Curse of Marit Lage leaves")
    void islandsUntapAfterCurseLeaves() {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfMaritLage());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        island.tap();
        forest.tap();

        gd.playerBattlefields.get(player1.getId()).remove(curse);

        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }
}
