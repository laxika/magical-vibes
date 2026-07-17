package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, tenCardLibrary());
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, tenCardLibrary());
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
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

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, tenCardLibrary());
        harness.setLibrary(player1, tenCardLibrary());
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does NOT trigger when the controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new MemoryErosion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, tenCardLibrary());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
