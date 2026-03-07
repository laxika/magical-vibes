package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RedSunsZenith;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryShuffleResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // resolveShuffleIntoLibrary (via RedSunsZenith)
    // =========================================================================

    @Nested
    @DisplayName("resolveShuffleIntoLibrary")
    class ResolveShuffleIntoLibrary {

        @Test
        @DisplayName("Card is shuffled into library instead of going to graveyard")
        void cardShuffledIntoLibrary() {
            harness.setHand(player1, List.of(new RedSunsZenith()));
            harness.addMana(player1, ManaColor.RED, 4);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 3, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Red Sun's Zenith"));
            assertThat(gd.playerDecks.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Red Sun's Zenith"));
        }

        @Test
        @DisplayName("Shuffle log is recorded")
        void shuffleLogRecorded() {
            harness.setHand(player1, List.of(new RedSunsZenith()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 1, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffled into its owner's library"));
        }
    }

    // =========================================================================
    // resolveShuffleGraveyardIntoLibrary (via Reminisce)
    // =========================================================================

    @Nested
    @DisplayName("resolveShuffleGraveyardIntoLibrary")
    class ResolveShuffleGraveyardIntoLibrary {

        @Test
        @DisplayName("Shuffles graveyard into library")
        void shufflesGraveyardIntoLibrary() {
            Card bear1 = new GrizzlyBears();
            Card bear2 = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bear1, bear2));
            harness.setHand(player1, List.of(new Reminisce()));
            harness.addMana(player1, ManaColor.BLUE, 3);
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            harness.castSorcery(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.gameLog).anyMatch(log -> log.contains("shuffles their graveyard"));
        }

        @Test
        @DisplayName("Empty graveyard still shuffles library")
        void emptyGraveyardStillShuffles() {
            harness.setGraveyard(player1, new ArrayList<>());
            harness.setHand(player1, List.of(new Reminisce()));
            harness.addMana(player1, ManaColor.BLUE, 3);
            int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

            harness.castSorcery(player1, 0, player1.getId());
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
            assertThat(gd.gameLog).anyMatch(log -> log.contains("graveyard is empty"));
        }
    }
}
