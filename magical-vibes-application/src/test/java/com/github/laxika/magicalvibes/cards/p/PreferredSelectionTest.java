package com.github.laxika.magicalvibes.cards.p;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PreferredSelectionTest extends BaseCardTest {

    private Card top;
    private Card second;

    private void setup() {
        harness.addToBattlefield(player1, new PreferredSelection());
        top = new GrizzlyBears();
        second = new LlanowarElves();
        gd.playerDecks.put(player1.getId(), new ArrayList<>(List.of(top, second, new Plains(), new Plains())));
    }

    private void triggerUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger
    }

    private void chooseLibraryCard(int index) {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    @Test
    void upkeepOffersTheSacrificeAndPayChoice() {
        setup();

        triggerUpkeep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void payingSacrificesTheEnchantmentAndPutsAChosenCardIntoHand() {
        setup();

        triggerUpkeep();
        // Added after the step advance — mana pools empty between steps.
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard(1);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(top);
        harness.assertNotOnBattlefield(player1, "Preferred Selection");
    }

    @Test
    void decliningBottomsOneOfTheTwoCardsAndKeepsTheEnchantment() {
        setup();

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, false);
        // The pick names the card that stays on top; the other one goes to the bottom.
        chooseLibraryCard(1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(second);
        assertThat(deck.getLast()).isSameAs(top);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, second);
        harness.assertOnBattlefield(player1, "Preferred Selection");
    }

    @Test
    void withoutManaTheDeclineBranchStillBottomsACard() {
        setup();

        triggerUpkeep();
        harness.handleMayAbilityChosen(player1, true);
        chooseLibraryCard(0);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(top);
        assertThat(deck.getLast()).isSameAs(second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top, second);
        harness.assertOnBattlefield(player1, "Preferred Selection");
    }
}
