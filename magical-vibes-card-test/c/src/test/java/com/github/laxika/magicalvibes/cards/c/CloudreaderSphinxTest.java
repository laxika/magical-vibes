package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudreaderSphinx.class})
class CloudreaderSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cloudreader Sphinx puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cloudreader Sphinx");
    }

    @Test
    @DisplayName("Resolving Cloudreader Sphinx enters battlefield and triggers ETB scry")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Cloudreader Sphinx");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Cloudreader Sphinx");
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 2 cards")
    void resolvingEtbEntersScryState() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 keeping both cards on top preserves them in order")
    void scryBothOnTop() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(deck.get(0)).isSameAs(originalTop0);
        assertThat(deck.get(1)).isSameAs(originalTop1);
    }

    @Test
    @DisplayName("Scry 2 putting both cards on bottom moves them to bottom")
    void scryBothOnBottom() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(deck.get(0)).isNotSameAs(originalTop0);
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 2)).isSameAs(originalTop0);
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop1);
    }

    @Test
    @DisplayName("Scry 2 putting one on top and one on bottom splits correctly")
    void scrySplitTopAndBottom() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Keep card 1 on top, put card 0 on bottom
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(deck.get(0)).isSameAs(originalTop1);
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop0);
    }

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Scry can reverse both cards on top without changing the rest of either library")
    void scryReordersTopCards() {
        Card first = new CloudreaderSphinx();
        Card second = new CloudreaderSphinx();
        Card third = new CloudreaderSphinx();
        Card opponentCard = new CloudreaderSphinx();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("Scry can reverse both cards on the bottom")
    void scryReordersBottomCards() {
        Card first = new CloudreaderSphinx();
        Card second = new CloudreaderSphinx();
        Card third = new CloudreaderSphinx();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    @DisplayName("Scry 2 with one card looks at only that card")
    void scryWithOneCard() {
        Card onlyCard = new CloudreaderSphinx();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Scry 2 with an empty library completes without a choice")
    void scryWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cloudreader Sphinx");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
