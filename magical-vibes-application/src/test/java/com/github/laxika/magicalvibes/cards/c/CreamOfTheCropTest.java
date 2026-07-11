package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CreamOfTheCropTest extends BaseCardTest {

    private void setLibraryTop(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    // ===== Look at top X (X = entering creature's power), keep one on top, rest to bottom =====

    @Test
    @DisplayName("Accepting the look puts the chosen card on top and the rest on the bottom")
    void keepsChosenOnTopRestOnBottom() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        // Top of library: Llanowar Elves, Shock, Plains.
        setLibraryTop(List.of(new LlanowarElves(), new Shock(), new Plains()));

        // Grizzly Bears (2/2, power 2) enters — look at the top 2 cards.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears (it enters)
        harness.passBothPriorities(); // resolve the MayEffect the trigger queued

        harness.handleMayAbilityChosen(player1, true); // accept the look

        // Look at top 2 [Llanowar Elves, Shock]; keep Shock on top (index 1), Llanowar to bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleLibraryCardChosen(gd, player1, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst().getName()).isEqualTo("Shock");
        assertThat(deck.getLast().getName()).isEqualTo("Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== X scales with the entering creature's power =====

    @Test
    @DisplayName("Looks at a number of cards equal to the entering creature's power")
    void looksAtCardsEqualToPower() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        setLibraryTop(List.of(new LlanowarElves(), new Shock(), new Plains(), new GrizzlyBears()));

        // Hill Giant (3/3, power 3) enters — look at the top 3 cards.
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Hill Giant
        harness.passBothPriorities(); // resolve the MayEffect

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(3);
    }

    // ===== Declining leaves the library untouched =====

    @Test
    @DisplayName("Declining the look leaves the library order unchanged")
    void decliningLeavesLibraryUnchanged() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        LlanowarElves topCard = new LlanowarElves();
        setLibraryTop(List.of(topCard, new Shock(), new Plains()));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Only triggers for creatures the controller controls =====

    @Test
    @DisplayName("Does not trigger for an opponent's creature entering")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new CreamOfTheCrop());
        setLibraryTop(List.of(new LlanowarElves(), new Shock(), new Plains()));

        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
