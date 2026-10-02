package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForcedFruition.class, GoldmeadowHarrier.class, Island.class})
class ForcedFruitionTest extends BaseCardTest {

    private List<com.github.laxika.magicalvibes.model.Card> tenCardLibrary() {
        var cards = new ArrayList<com.github.laxika.magicalvibes.model.Card>();
        for (int i = 0; i < 10; i++) {
            cards.add(new GoldmeadowHarrier());
        }
        return cards;
    }

    @Test
    @DisplayName("Opponent casting a spell puts triggered ability on stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("The casting opponent draws seven cards")
    void castingOpponentDrawsSeven() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}"); // hand -> empty after cast
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 7);
    }

    @Test
    @DisplayName("The controller of Forced Fruition does not draw")
    void controllerDoesNotDraw() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());
        harness.setHand(player1, List.of());

        harness.castFromHand(player2, new GoldmeadowHarrier(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.castFromHand(player1, new GoldmeadowHarrier(), "{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Does NOT trigger when an opponent plays a land")
    void doesNotTriggerOnOpponentLandPlay() {
        harness.addToBattlefield(player1, new ForcedFruition());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Island()));
        harness.playLand(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
