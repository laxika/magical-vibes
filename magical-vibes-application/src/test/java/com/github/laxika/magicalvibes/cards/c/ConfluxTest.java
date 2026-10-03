package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.a.AvenMindcensor;
import com.github.laxika.magicalvibes.cards.o.ObNixilisUnshackled;
import com.github.laxika.magicalvibes.cards.f.FaerieMechanist;
import com.github.laxika.magicalvibes.cards.e.EmberWeaver;
import com.github.laxika.magicalvibes.cards.h.HellsparkElemental;
import com.github.laxika.magicalvibes.cards.r.RottingRats;
import com.github.laxika.magicalvibes.cards.p.Progenitus;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.LibrarySearchFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Conflux.class, AvenSquire.class, FaerieMechanist.class, RottingRats.class,
        HellsparkElemental.class, EmberWeaver.class, Progenitus.class,
        AvenMindcensor.class, ObNixilisUnshackled.class})
class ConfluxTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Conflux first offers only white cards, revealed, to hand")
    void firstPickIsWhiteToHand() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Aven Squire");
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().followUp().remainingToHandPicks())
                .extracting(LibrarySearchFollowUp.ToHandPick::color)
                .containsExactly(CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Picking each colour puts one card of every colour into hand and shuffles")
    void picksOneOfEachColorToHand() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // White, then blue, then black, then red, then green — one card each.
        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Aven Squire", "Faerie Mechanist", "Rotting Rats", "Hellspark Elemental", "Ember Weaver");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("Second pick offers only blue cards after the white pick")
    void secondPickIsBlue() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        harness.handleCardChosen(player1, 0);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Faerie Mechanist");
        assertThat(search.params().followUp().remainingToHandPicks())
                .extracting(LibrarySearchFollowUp.ToHandPick::color)
                .containsExactly(CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("A colour absent from the library is skipped without a pick")
    void absentColorIsSkipped() {
        setupAndCast();
        // No black card in the library.
        harness.setLibrary(player1, List.of(new AvenSquire(), new FaerieMechanist(),
                new HellsparkElemental(), new EmberWeaver()));

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // White, then blue — the next pick should skip black straight to red.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Hellspark Elemental");
    }

    @Test
    @DisplayName("Failing to find a colour takes no card and continues to the next colour")
    void mayFailToFindColor() {
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        GameData gd = harness.getGameData();

        // Decline the white pick, then take the remaining four colours.
        harness.handleCardChosen(player1, -1);
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Aven Squire")
                .contains("Faerie Mechanist", "Rotting Rats", "Hellspark Elemental", "Ember Weaver");
    }

    @Test
    @DisplayName("Empty library resolves without a search interaction")
    void emptyLibrary() {
        setupAndCast();
        harness.setLibrary(player1, List.of());

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(e -> e.contains("it is empty"));
    }

    @Test
    @DisplayName("Five different multicolored cards can fill the five color choices")
    void findsFiveMulticoloredCards() {
        setupAndCast();
        List<Card> cards = List.of(new Progenitus(), new Progenitus(), new Progenitus(),
                new Progenitus(), new Conflux());
        harness.setLibrary(player1, cards);

        harness.passBothPriorities();
        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, 0);
        }

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A multicolored card can fill a later color after declining earlier colors")
    void findsMulticoloredCardForLaterColor() {
        setupAndCast();
        Progenitus card = new Progenitus();
        harness.setLibrary(player1, List.of(card));

        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, -1);
        }
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining every color leaves the cards in the library and still shuffles")
    void mayFailToFindEveryColor() {
        setupAndCast();
        setupFullColorLibrary();
        GameData gd = harness.getGameData();
        List<Card> original = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.passBothPriorities();
        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, -1);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(original);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .filter(e -> e.contains("shuffled")).hasSize(1);
    }

    @Test
    @DisplayName("Conflux is one library search and triggers Ob Nixilis only once")
    void triggersOnceForTheWholeSearch() {
        harness.addToBattlefield(player2, new ObNixilisUnshackled());
        harness.setLife(player1, 100);
        setupAndCast();
        setupFullColorLibrary();

        harness.passBothPriorities();
        for (int i = 0; i < 5; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(harness.getGameData().stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(harness.getGameData().getLife(player1.getId())).isEqualTo(90);
    }

    @Test
    @DisplayName("Mindcensor does not end the search when the top four contain no white card")
    void continuesToOtherColorsWithinMindcensorLimit() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        setupAndCast();
        List<Card> searchable = List.of(new FaerieMechanist(), new RottingRats(),
                new HellsparkElemental(), new EmberWeaver());
        AvenSquire outsideLimit = new AvenSquire();
        harness.setLibrary(player1, List.of(searchable.get(0), searchable.get(1),
                searchable.get(2), searchable.get(3), outsideLimit));

        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class))
                .isNotNull();
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(searchable);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(outsideLimit);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(e -> e.contains("shuffled"));
    }

    @Test
    @DisplayName("Mindcensor restricts the entire search to the original top four cards")
    void doesNotExposeDeeperCardsAfterEarlierPicks() {
        harness.addToBattlefield(player2, new AvenMindcensor());
        setupAndCast();
        AvenSquire white = new AvenSquire();
        FaerieMechanist blue = new FaerieMechanist();
        RottingRats black = new RottingRats();
        HellsparkElemental red = new HellsparkElemental();
        EmberWeaver outsideLimit = new EmberWeaver();
        harness.setLibrary(player1, List.of(white, blue, black, red, outsideLimit));

        harness.passBothPriorities();
        for (int i = 0; i < 4; i++) {
            harness.handleCardChosen(player1, 0);
        }

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(white, blue, black, red);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(outsideLimit);
    }

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
        harness.setLibrary(player1, List.of(new AvenSquire(), new FaerieMechanist(), new RottingRats(),
                new HellsparkElemental(), new EmberWeaver()));
    }
}
