package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConundrumSphinx.class, LightningBolt.class, RuneclawBear.class})
class ConundrumSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts ON_ATTACK trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new ConundrumSphinx());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Conundrum Sphinx"));
    }

    @Test
    @DisplayName("Resolving trigger prompts active player to name a card first")
    void resolvingPromptsActivePlayerFirst() {
        addCreatureReady(player1, new ConundrumSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.EachPlayerCardNameRevealChoice.class);
    }

    @Test
    @DisplayName("After active player names, opponent is prompted to name a card")
    void afterActivePlayerNamesOpponentIsPrompted() {
        addCreatureReady(player1, new ConundrumSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOf(ChoiceContext.EachPlayerCardNameRevealChoice.class);

        var ctx = (ChoiceContext.EachPlayerCardNameRevealChoice) gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context();
        assertThat(ctx.chosenNames()).containsEntry(player1.getId(), "Lightning Bolt");
    }

    @Test
    @DisplayName("If a player guesses correctly, the top card goes to their hand")
    void correctGuessGoesToHand() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Set up known top card for player1
        Card knownCard = new LightningBolt();
        harness.setLibrary(player1, List.of(knownCard, new ConundrumSphinx()));
        // Set up known top card for player2
        Card opponentTopCard = new RuneclawBear();
        harness.setLibrary(player2, List.of(opponentTopCard, new ConundrumSphinx()));

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Lightning Bolt"); // correct guess
        harness.handleListChoice(player2, "Conundrum Sphinx"); // wrong guess

        // Player1 guessed correctly — Lightning Bolt should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        harness.assertInHand(player1, "Lightning Bolt");
        // Lightning Bolt should no longer be on top of deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(knownCard.getId()));
    }

    @Test
    @DisplayName("If a player guesses wrong, the top card goes to bottom of library")
    void wrongGuessGoesToBottom() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Set up known top card for player1
        Card knownCard = new LightningBolt();
        harness.setLibrary(player1, List.of(knownCard, new ConundrumSphinx()));
        // Set up known top card for player2
        Card opponentTopCard = new RuneclawBear();
        harness.setLibrary(player2, List.of(opponentTopCard, new ConundrumSphinx()));

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p1DeckSize = gd.playerDecks.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Conundrum Sphinx"); // wrong guess
        harness.handleListChoice(player2, "Conundrum Sphinx"); // wrong guess

        // Player1 guessed wrong — hand should not grow
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Deck size should stay the same (card moved from top to bottom)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckSize);
        // The card should be at the bottom of the deck
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId())
                .isEqualTo(knownCard.getId());
    }

    @Test
    @DisplayName("Both players can guess correctly and both get cards")
    void bothPlayersCorrect() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = new LightningBolt();
        harness.setLibrary(player1, List.of(p1Top, new ConundrumSphinx()));
        Card p2Top = new RuneclawBear();
        harness.setLibrary(player2, List.of(p2Top, new ConundrumSphinx()));

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Runeclaw Bear");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    @Test
    @DisplayName("Empty library does not crash — player just gets a log message")
    void emptyLibraryHandledGracefully() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Clear player1's deck
        harness.setLibrary(player1, List.of());
        // Set a card for player2
        Card p2Top = new RuneclawBear();
        harness.setLibrary(player2, List.of(p2Top, new ConundrumSphinx()));

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Runeclaw Bear");

        // Player1 had empty library — hand unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Player2 guessed correctly
        harness.assertInHand(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Card name choices and reveals are logged")
    void choicesAndRevealsAreLogged() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = new LightningBolt();
        harness.setLibrary(player1, List.of(p1Top, new ConundrumSphinx()));
        Card p2Top = new RuneclawBear();
        harness.setLibrary(player2, List.of(p2Top, new ConundrumSphinx()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Conundrum Sphinx");

        // Verify name choices are logged
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses \"Lightning Bolt\""));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("chooses \"Conundrum Sphinx\""));
        // Verify reveals are logged
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals Lightning Bolt"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals Runeclaw Bear"));
    }

    @Test
    @DisplayName("Interaction state clears after both players have named and reveal resolves")
    void interactionClearsAfterResolve() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = new LightningBolt();
        harness.setLibrary(player1, List.of(p1Top, new ConundrumSphinx()));
        Card p2Top = new RuneclawBear();
        harness.setLibrary(player2, List.of(p2Top, new ConundrumSphinx()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Runeclaw Bear");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }


    @Test
    @DisplayName("No library card moves or is revealed until both players choose names")
    void allNamesChosenBeforeRevealing() {
        addCreatureReady(player1, new ConundrumSphinx());
        Card top = new LightningBolt();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(new RuneclawBear()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("reveals Lightning Bolt"));

        harness.handleListChoice(player2, "Runeclaw Bear");
        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInHand(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Name suggestions do not disclose cards in hidden zones")
    void nameSuggestionsDoNotDependOnHiddenCards() {
        addCreatureReady(player1, new ConundrumSphinx());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new ConundrumSphinx()));
        harness.setLibrary(player2, List.of(new ConundrumSphinx()));
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        var before = List.copyOf(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options());

        harness.setLibrary(player2, List.of(new LightningBolt(), new RuneclawBear()));
        harness.handleListChoice(player1, "Conundrum Sphinx");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyElementsOf(before);
        harness.handleListChoice(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("A nonexistent card name cannot be chosen")
    void rejectsNonexistentCardName() {
        addCreatureReady(player1, new ConundrumSphinx());
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Not A Real Oracle Card 987654321"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The attack trigger resolves even after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        addCreatureReady(player1, new ConundrumSphinx());
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.setLibrary(player2, List.of(new RuneclawBear()));
        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Runeclaw Bear");

        harness.assertInHand(player1, "Lightning Bolt");
        harness.assertInHand(player2, "Runeclaw Bear");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both players still choose names with empty libraries and neither draws")
    void bothLibrariesEmpty() {
        addCreatureReady(player1, new ConundrumSphinx());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        int firstHandSize = gd.playerHands.get(player1.getId()).size();
        int secondHandSize = gd.playerHands.get(player2.getId()).size();
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Conundrum Sphinx");
        harness.handleListChoice(player2, "Conundrum Sphinx");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(firstHandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(secondHandSize);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
