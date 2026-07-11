package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class GameSetupServiceAllRandomTest {

    private GameSetupService gameSetupService;
    private GameRegistry gameRegistry;
    private Player creator;
    private Player joiner;

    @BeforeEach
    void setUp() {
        GameTestHarness harness = new GameTestHarness();
        gameSetupService = harness.getGameSetupService();
        gameRegistry = harness.getGameRegistry();
        creator = new Player(UUID.randomUUID(), "Carol");
        joiner = new Player(UUID.randomUUID(), "Dave");
    }

    @Test
    void allRandomGameDealsGeneratedDecksAndIgnoresDeckChoices() {
        GameData gd = gameSetupService.createGame("All Random", creator, "cho-mannos-resolve", true);
        gameSetupService.joinGame(gd, joiner, "kamahls-temper");

        assertThat(gd.allRandom).isTrue();
        assertThat(gd.status).isEqualTo(GameStatus.MULLIGAN);
        assertThat(gd.playerDeckChoices)
                .containsEntry(creator.getId(), RandomDeckGenerator.RANDOM_DECK_ID)
                .containsEntry(joiner.getId(), RandomDeckGenerator.RANDOM_DECK_ID);

        for (Player player : List.of(creator, joiner)) {
            List<Card> allCards = new ArrayList<>(gd.playerHands.get(player.getId()));
            allCards.addAll(gd.playerDecks.get(player.getId()));

            assertThat(gd.playerHands.get(player.getId())).hasSize(7);
            assertThat(allCards).hasSize(RandomDeckGenerator.DECK_SIZE);
            assertThat(allCards).allSatisfy(card -> assertThat(card.getOwnerId()).isEqualTo(player.getId()));

            long basicLands = allCards.stream()
                    .filter(c -> c.getSupertypes().contains(CardSupertype.BASIC) && c.hasType(CardType.LAND))
                    .count();
            assertThat(basicLands).isEqualTo(RandomDeckGenerator.LAND_COUNT);
        }

        // The two decks are generated independently — they must not share card instances
        assertThat(gd.playerDecks.get(creator.getId()))
                .isNotSameAs(gd.playerDecks.get(joiner.getId()));

        assertThat(gd.gameLog)
                .contains("Carol is playing with a randomly generated deck.")
                .contains("Dave is playing with a randomly generated deck.");

        gameRegistry.remove(gd.id);
    }

    @Test
    void standardGameStillResolvesPrebuiltDecks() {
        GameData gd = gameSetupService.createGame("Normal", creator, "cho-mannos-resolve", false);
        gameSetupService.joinGame(gd, joiner, "kamahls-temper");

        assertThat(gd.allRandom).isFalse();
        assertThat(gd.playerDeckChoices)
                .containsEntry(creator.getId(), "cho-mannos-resolve")
                .containsEntry(joiner.getId(), "kamahls-temper");
        assertThat(gd.gameLog)
                .contains("Carol is playing with Cho-Manno's Resolve (Tenth Edition Theme Deck).");

        gameRegistry.remove(gd.id);
    }
}
