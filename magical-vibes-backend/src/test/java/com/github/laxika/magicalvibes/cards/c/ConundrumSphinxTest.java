package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EachPlayerNameCardRevealTopEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConundrumSphinxTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ON_ATTACK trigger with EachPlayerNameCardRevealTopEffect")
    void hasCorrectStructure() {
        ConundrumSphinx card = new ConundrumSphinx();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(EachPlayerNameCardRevealTopEffect.class);
    }

    // ===== Attack trigger =====

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

    // ===== Card name choice flow =====

    @Test
    @DisplayName("Resolving trigger prompts active player to name a card first")
    void resolvingPromptsActivePlayerFirst() {
        addCreatureReady(player1, new ConundrumSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.colorChoice().choiceContext())
                .isInstanceOf(ChoiceContext.EachPlayerCardNameRevealChoice.class);
    }

    @Test
    @DisplayName("After active player names, opponent is prompted to name a card")
    void afterActivePlayerNamesOpponentIsPrompted() {
        addCreatureReady(player1, new ConundrumSphinx());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Lightning Bolt");

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player2.getId());
        assertThat(gd.interaction.colorChoice().choiceContext())
                .isInstanceOf(ChoiceContext.EachPlayerCardNameRevealChoice.class);

        var ctx = (ChoiceContext.EachPlayerCardNameRevealChoice) gd.interaction.colorChoice().choiceContext();
        assertThat(ctx.chosenNames()).containsEntry(player1.getId(), "Lightning Bolt");
    }

    // ===== Correct guess — card goes to hand =====

    @Test
    @DisplayName("If a player guesses correctly, the top card goes to their hand")
    void correctGuessGoesToHand() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Set up known top card for player1
        Card knownCard = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player1.getId()).addFirst(knownCard);
        // Set up known top card for player2
        Card opponentTopCard = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(opponentTopCard);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Lightning Bolt"); // correct guess
        harness.handleListChoice(player2, "Wrong Card"); // wrong guess

        // Player1 guessed correctly — Lightning Bolt should be in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        // Lightning Bolt should no longer be on top of deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(knownCard.getId()));
    }

    // ===== Wrong guess — card goes to bottom =====

    @Test
    @DisplayName("If a player guesses wrong, the top card goes to bottom of library")
    void wrongGuessGoesToBottom() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Set up known top card for player1
        Card knownCard = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player1.getId()).addFirst(knownCard);
        // Set up known top card for player2
        Card opponentTopCard = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(opponentTopCard);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p1DeckSize = gd.playerDecks.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve trigger

        harness.handleListChoice(player1, "Wrong Guess"); // wrong guess
        harness.handleListChoice(player2, "Wrong Card"); // wrong guess

        // Player1 guessed wrong — hand should not grow
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Deck size should stay the same (card moved from top to bottom)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckSize);
        // The card should be at the bottom of the deck
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId())
                .isEqualTo(knownCard.getId());
    }

    // ===== Both players guess correctly =====

    @Test
    @DisplayName("Both players can guess correctly and both get cards")
    void bothPlayersCorrect() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player1.getId()).addFirst(p1Top);
        Card p2Top = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(p2Top);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Empty library does not crash — player just gets a log message")
    void emptyLibraryHandledGracefully() {
        addCreatureReady(player1, new ConundrumSphinx());

        // Clear player1's deck
        gd.playerDecks.get(player1.getId()).clear();
        // Set a card for player2
        Card p2Top = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(p2Top);

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Grizzly Bears");

        // Player1 had empty library — hand unchanged
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore);
        // Player2 guessed correctly
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Card name choices and reveals are logged")
    void choicesAndRevealsAreLogged() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player1.getId()).addFirst(p1Top);
        Card p2Top = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(p2Top);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Wrong Card");

        // Verify name choices are logged
        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses \"Lightning Bolt\""));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses \"Wrong Card\""));
        // Verify reveals are logged
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals Lightning Bolt"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("reveals Grizzly Bears"));
    }

    // ===== Interaction clears after both name =====

    @Test
    @DisplayName("Interaction state clears after both players have named and reveal resolves")
    void interactionClearsAfterResolve() {
        addCreatureReady(player1, new ConundrumSphinx());

        Card p1Top = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player1.getId()).addFirst(p1Top);
        Card p2Top = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(p2Top);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Lightning Bolt");
        harness.handleListChoice(player2, "Grizzly Bears");

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.colorChoice()).isNull();
    }

    // ===== Helpers =====

    private Permanent addCreatureReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        return card;
    }
}
