package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfMaritLageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering taps every Island on every battlefield but leaves other lands alone")
    void entryTapsAllIslands() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new CurseOfMaritLage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve the enchantment → ETB trigger onto the stack
        harness.passBothPriorities(); // resolve the trigger

        assertThat(ownIsland.isTapped()).isTrue();
        assertThat(opponentIsland.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Islands stay tapped through their controller's untap step")
    void islandsDoNotUntap() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        island.tap();
        forest.tap();
        harness.addToBattlefield(player2, new CurseOfMaritLage());

        // player2 ends their turn so player1's untap step actually runs.
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // cascade into player1's untap step
        harness.clearPriorityPassed();
        harness.passBothPriorities();

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

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentIsland.isTapped()).isTrue();
        assertThat(opponentForest.isTapped()).isFalse();
    }
}
