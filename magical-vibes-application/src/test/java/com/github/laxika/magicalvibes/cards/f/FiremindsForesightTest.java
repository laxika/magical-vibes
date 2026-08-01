package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FiremindsForesightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving first offers only mana-value-3 instants, revealed, to hand")
    void firstPickIsManaValue3InstantToHand() {
        setupAndCast();
        setupFullLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Cancel");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().followUp().remainingInstantManaValueToHandPicks())
                .containsExactly(2, 1);
    }

    @Test
    @DisplayName("Picking each mana value puts those instants into hand and shuffles once")
    void picksInstantsWithManaValues3Then2Then1() {
        setupAndCast();
        setupFullLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        for (int i = 0; i < 3; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Cancel", "Negate", "Shock");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("Second pick offers only mana-value-2 instants after the mana-value-3 pick")
    void secondPickIsManaValue2() {
        setupAndCast();
        setupFullLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Negate");
        assertThat(search.params().followUp().remainingInstantManaValueToHandPicks())
                .containsExactly(1);
    }

    @Test
    @DisplayName("A mana value absent from the library is skipped without a pick")
    void absentManaValueIsSkipped() {
        setupAndCast();
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Cancel(), new Shock(), new EliteVanguard()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Shock");
    }

    @Test
    @DisplayName("Failing to find a mana value takes no card and continues to the next")
    void mayFailToFindManaValue() {
        setupAndCast();
        setupFullLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        for (int i = 0; i < 2; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Cancel")
                .contains("Negate", "Shock");
    }

    @Test
    @DisplayName("Empty library resolves without a search interaction")
    void emptyLibrary() {
        setupAndCast();
        harness.getGameData().playerDecks.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("it is empty"));
    }

    private void setupAndCast() {
        harness.setHand(player1, List.of(new FiremindsForesight()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0);
    }

    private void setupFullLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        // Cancel MV 3, Negate MV 2, Shock MV 1; Elite Vanguard is a creature (must not be offered).
        deck.addAll(List.of(new Cancel(), new Negate(), new Shock(), new EliteVanguard()));
    }
}
