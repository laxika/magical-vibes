package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ZoneToLibraryServiceTest {

    @Mock
    private GraveyardService graveyardService;

    @InjectMocks
    private ZoneToLibraryService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
    }

    private static Card card(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private static List<Card> cards(String... names) {
        List<Card> list = new ArrayList<>();
        for (String name : names) {
            list.add(card(name));
        }
        return list;
    }

    private static List<String> namesOf(List<Card> cards) {
        return cards.stream().map(Card::getName).toList();
    }

    @Test
    @DisplayName("Drains both zones into the library and reports what each contributed")
    void drainsBothZonesAndReportsCounts() {
        gd.playerDecks.put(player1Id, cards("Library A"));
        gd.playerHands.put(player1Id, cards("Hand A", "Hand B"));
        gd.playerGraveyards.put(player1Id, cards("Grave A", "Grave B", "Grave C"));

        ZoneToLibraryService.MovedCounts moved = sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        assertThat(moved.hand()).isEqualTo(2);
        assertThat(moved.graveyard()).isEqualTo(3);
        assertThat(gd.playerDecks.get(player1Id)).hasSize(6);
        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
    }

    @Test
    @DisplayName("Appends to the existing library without shuffling — that is the caller's job")
    void appendsWithoutShuffling() {
        gd.playerDecks.put(player1Id, cards("Library A"));
        gd.playerHands.put(player1Id, cards("Hand A"));
        gd.playerGraveyards.put(player1Id, cards("Grave A"));

        sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        // One caller shuffles per player, another shuffles once after also tucking permanents, so
        // this must leave the order alone and never shuffle on its own.
        assertThat(namesOf(gd.playerDecks.get(player1Id)))
                .containsExactly("Library A", "Hand A", "Grave A");
    }

    @Test
    @DisplayName("Notifies graveyard-departure watchers when the graveyard gave up cards")
    void notifiesWhenGraveyardGaveUpCards() {
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerGraveyards.put(player1Id, cards("Grave A"));

        sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        // The whole reason this move is shared: one of the two callers used to skip this, leaving
        // every "if one or more cards left your graveyard this turn" reader stale.
        verify(graveyardService).notifyCardsLeftGraveyard(eq(gd), eq(player1Id), anyList());
    }

    @Test
    @DisplayName("Does not notify when the graveyard contributed nothing")
    void doesNotNotifyWhenGraveyardContributedNothing() {
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player1Id, cards("Hand A"));
        gd.playerGraveyards.put(player1Id, new ArrayList<>());

        ZoneToLibraryService.MovedCounts moved = sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        assertThat(moved.hand()).isEqualTo(1);
        assertThat(moved.graveyard()).isZero();
        verify(graveyardService, never()).notifyCardsLeftGraveyard(any(), any());
    }

    @Test
    @DisplayName("Tolerates a player with no hand or graveyard zone at all")
    void toleratesAbsentZones() {
        gd.playerDecks.put(player1Id, cards("Library A"));

        ZoneToLibraryService.MovedCounts moved = sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        assertThat(moved.hand()).isZero();
        assertThat(moved.graveyard()).isZero();
        assertThat(gd.playerDecks.get(player1Id)).hasSize(1);
        verify(graveyardService, never()).notifyCardsLeftGraveyard(any(), any());
    }

    @Test
    @DisplayName("Does nothing when the player has no library to move cards into")
    void doesNothingWithoutALibrary() {
        gd.playerHands.put(player1Id, cards("Hand A"));
        gd.playerGraveyards.put(player1Id, cards("Grave A"));

        ZoneToLibraryService.MovedCounts moved = sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        assertThat(moved.hand()).isZero();
        assertThat(moved.graveyard()).isZero();
        // Cards must not be dropped on the floor when there is nowhere to put them.
        assertThat(gd.playerHands.get(player1Id)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1Id)).hasSize(1);
        verify(graveyardService, never()).notifyCardsLeftGraveyard(any(), any());
    }

    @Test
    @DisplayName("Leaves the other player's zones untouched")
    void leavesOtherPlayersZonesAlone() {
        gd.playerDecks.put(player1Id, new ArrayList<>());
        gd.playerHands.put(player1Id, cards("Hand A"));
        gd.playerGraveyards.put(player1Id, cards("Grave A"));

        gd.playerDecks.put(player2Id, cards("Their Library"));
        gd.playerHands.put(player2Id, cards("Their Hand"));
        gd.playerGraveyards.put(player2Id, cards("Their Grave"));

        sut.moveHandAndGraveyardIntoLibrary(gd, player1Id);

        assertThat(namesOf(gd.playerDecks.get(player2Id))).containsExactly("Their Library");
        assertThat(namesOf(gd.playerHands.get(player2Id))).containsExactly("Their Hand");
        assertThat(namesOf(gd.playerGraveyards.get(player2Id))).containsExactly("Their Grave");
        verify(graveyardService, never()).notifyCardsLeftGraveyard(gd, player2Id);
    }
}
