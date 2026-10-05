package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvincibleHymn.class, CylianElf.class})
class InvincibleHymnTest extends BaseCardTest {

    private void cast(int librarySize) {
        harness.setLibrary(player1,
                IntStream.range(0, librarySize).mapToObj(i -> new CylianElf()).toList());
        harness.setHand(player1, List.of(new InvincibleHymn()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Controller's life total becomes the number of cards in their library (lowering it)")
    void lowersLifeToLibrarySize() {
        harness.setLife(player1, 20);

        cast(7);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Controller's life total becomes the number of cards in their library (raising it)")
    void raisesLifeToLibrarySize() {
        harness.setLife(player1, 3);

        cast(40);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("An empty library sets the controller's life total to 0")
    void emptyLibrarySetsLifeToZero() {
        harness.setLife(player1, 20);

        cast(0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the controller's own library is counted, not the opponent's")
    void countsOnlyControllerLibrary() {
        harness.setLife(player1, 20);
        harness.setLibrary(player2,
                List.of(new CylianElf(), new CylianElf(), new CylianElf()));

        cast(5);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("An equal library size leaves the life total unchanged without gaining life")
    void equalLibrarySizeLeavesLifeUnchanged() {
        harness.setLife(player1, 7);

        cast(7);

        harness.assertLife(player1, 7);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The library is counted at resolution and its cards remain in the library")
    void countsLibraryAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 13);
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.setHand(player1, List.of(new InvincibleHymn()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castSorcery(player1, 0, 0);

        harness.setLibrary(player1, List.of(new CylianElf(), new CylianElf(), new CylianElf()));
        harness.passBothPriorities();

        harness.assertLife(player1, 3);
        harness.assertLife(player2, 13);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Invincible Hymn");
    }
}
