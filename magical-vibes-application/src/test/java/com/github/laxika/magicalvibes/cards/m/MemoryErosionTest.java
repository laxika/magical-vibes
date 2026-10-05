package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MemoryErosion.class, GrizzlyBears.class, SuntailHawk.class, TrueBeliever.class})
class MemoryErosionTest extends BaseCardTest {

    private List<com.github.laxika.magicalvibes.model.Card> tenCardLibrary() {
        var cards = new ArrayList<com.github.laxika.magicalvibes.model.Card>();
        for (int i = 0; i < 10; i++) {
            cards.add(new SuntailHawk());
        }
        return cards;
    }

    @Test
    @DisplayName("Opponent casting a spell puts the mill trigger on the stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new MemoryErosion());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("The casting opponent mills two cards")
    void castingOpponentMillsTwo() {
        harness.addToBattlefield(player1, new MemoryErosion());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The controller's own library is not milled")
    void controllerNotMilled() {
        harness.addToBattlefield(player1, new MemoryErosion());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, tenCardLibrary());
        harness.setLibrary(player1, tenCardLibrary());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.setLibrary(player1, tenCardLibrary());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Milling does not target an opponent with shroud")
    void millsOpponentWithShroud() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.addToBattlefield(player2, new TrueBeliever());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Memory Erosion mills independently before the spell resolves")
    void multipleCopiesEachMillTwo() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player2, tenCardLibrary());

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Milling a one-card library puts only that card in the graveyard")
    void millsRemainingCardFromShortLibrary() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var remainingCard = new SuntailHawk();
        harness.setLibrary(player2, List.of(remainingCard));

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A noncreature spell triggers milling of the top two cards")
    void noncreatureSpellMillsTopTwoCards() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        var first = new SuntailHawk();
        var second = new GrizzlyBears();
        var third = new SuntailHawk();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.castFromHand(player2, new MemoryErosion(), "{1}{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).hasSize(1);
    }
}
