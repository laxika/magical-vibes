package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class InvincibleHymnTest extends BaseCardTest {

    private void cast(int librarySize) {
        harness.setLibrary(player1,
                IntStream.range(0, librarySize).mapToObj(i -> new GrizzlyBears()).collect(java.util.stream.Collectors.toList()));
        harness.setHand(player1, List.of(new InvincibleHymn()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
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
                List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        cast(5);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
    }
}
