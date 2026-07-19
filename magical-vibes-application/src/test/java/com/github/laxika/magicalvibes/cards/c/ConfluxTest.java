package com.github.laxika.magicalvibes.cards.c;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfluxTest extends BaseCardTest {

    // ===== First pick is a white-restricted, revealed search =====

    @Test
    @DisplayName("Resolving Conflux first offers only white cards, revealed, to hand")
    void firstPickIsWhiteToHand() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Elite Vanguard");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().followUp().remainingColorToHandPicks())
                .containsExactly(CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    // ===== Full flow: one card of each colour to hand =====

    @Test
    @DisplayName("Picking each colour puts one card of every colour into hand and shuffles")
    void picksOneOfEachColorToHand() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // White, then blue, then black, then red, then green — one card each.
        for (int i = 0; i < 5; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Elite Vanguard", "Fugitive Wizard", "Scathe Zombies", "Hill Giant", "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("Second pick offers only blue cards after the white pick")
    void secondPickIsBlue() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Fugitive Wizard");
        assertThat(search.params().followUp().remainingColorToHandPicks())
                .containsExactly(CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    // ===== A colour with no matching card is skipped =====

    @Test
    @DisplayName("A colour absent from the library is skipped without a pick")
    void absentColorIsSkipped() {
        setupAndCast();
        // No black card in the library.
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new EliteVanguard(), new FugitiveWizard(), new HillGiant(), new GrizzlyBears()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // White, then blue — the next pick should skip black straight to red.
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Hill Giant");
    }

    // ===== May fail to find a colour =====

    @Test
    @DisplayName("Failing to find a colour takes no card and continues to the next colour")
    void mayFailToFindColor() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Decline the white pick, then take the remaining four colours.
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        for (int i = 0; i < 4; i++) {
            harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Elite Vanguard")
                .contains("Fugitive Wizard", "Scathe Zombies", "Hill Giant", "Grizzly Bears");
    }

    // ===== Edge case: empty library =====

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

    // ===== Helpers =====

    private void setupAndCast() {
        harness.setHand(player1, List.of(new Conflux()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupFullColorLibrary() {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new EliteVanguard(), new FugitiveWizard(), new ScatheZombies(),
                new HillGiant(), new GrizzlyBears()));
    }
}
