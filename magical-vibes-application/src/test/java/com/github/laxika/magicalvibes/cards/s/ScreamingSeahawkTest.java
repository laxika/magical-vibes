package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScreamingSeahawk.class, GrizzlyBears.class})
class ScreamingSeahawkTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a may search prompt")
    void enteringTheBattlefieldCreatesMaySearchPrompt() {
        setupAndCast();

        resolveCreatureAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the may ability searches for a Screaming Seahawk")
    void acceptingMaySearchesForScreamingSeahawk() {
        setupAndCast();
        ScreamingSeahawk seahawk = new ScreamingSeahawk();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(seahawk, bears);

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(seahawk);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(seahawk);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningMayDoesNotSearch() {
        setupAndCast();
        setLibrary(new ScreamingSeahawk());

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The search offers only cards named Screaming Seahawk")
    void searchFiltersByName() {
        setupAndCast();
        setLibrary(new ScreamingSeahawk(), new GrizzlyBears());

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).hasSize(1)
                .allMatch(card -> card.getName().equals("Screaming Seahawk"));
    }

    @Test
    @DisplayName("An empty search has no eligible cards")
    void emptySearchHasNoEligibleCards() {
        setupAndCast();
        setLibrary(new GrizzlyBears());

        resolveCreatureAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(entry -> entry.contains("finds no cards named Screaming Seahawk"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new ScreamingSeahawk()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveCreatureAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
