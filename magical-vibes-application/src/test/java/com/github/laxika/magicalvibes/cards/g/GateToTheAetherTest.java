package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LumengridWarden;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GateToTheAether.class, WeldingJar.class, LumengridWarden.class, RuleOfLaw.class,
        Forest.class, Shatter.class})
class GateToTheAetherTest extends BaseCardTest {

    @Test
    @DisplayName("Each player reveals their own top card and may put a permanent onto the battlefield")
    void eachPlayerUsesTheirOwnLibrary() {
        harness.addToBattlefield(player1, new GateToTheAether());
        harness.setLibrary(player1, List.of(new WeldingJar(), new Shatter()));
        harness.setLibrary(player2, List.of(new LumengridWarden(), new Shatter()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Welding Jar");

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.assertOnBattlefield(player2, "Lumengrid Warden");
    }

    @Test
    @DisplayName("Offers artifact, creature, enchantment, and land cards")
    void offersAllPrintedPermanentTypes() {
        harness.addToBattlefield(player1, new GateToTheAether());
        List<Card> cards = List.of(new WeldingJar(), new LumengridWarden(), new RuleOfLaw(), new Forest());

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            harness.setLibrary(player1, List.of(card, new Shatter()));
            advanceToUpkeep(player1);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.assertOnBattlefield(player1, card.getName());
        }
    }

    @Test
    @DisplayName("Leaves a nonpermanent card on top without offering a choice")
    void nonpermanentCardStaysOnTop() {
        harness.addToBattlefield(player1, new GateToTheAether());
        Card topCard = new Shatter();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        harness.assertNotInHand(player1, "Shatter");
    }

    @Test
    @DisplayName("Leaves a matching card on top when declined")
    void declinedLeavesMatchingCardOnTop() {
        harness.addToBattlefield(player1, new GateToTheAether());
        Card topCard = new Forest();
        Card nextCard = new Shatter();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Does nothing when the active player's library is empty")
    void emptyLibraryProducesNoChoice() {
        harness.addToBattlefield(player1, new GateToTheAether());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
