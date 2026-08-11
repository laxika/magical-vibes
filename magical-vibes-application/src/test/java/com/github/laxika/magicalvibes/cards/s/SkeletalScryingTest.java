package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkeletalScryingTest extends BaseCardTest {

    private void cast(int xValue, List<Integer> exileIndices) {
        gs.playCard(gd, player1, 0, xValue, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, exileIndices);
    }

    @Test
    @DisplayName("Exiling two cards draws two cards and causes two life loss")
    void exilesTwoDrawsTwoAndLosesTwoLife() {
        harness.setGraveyard(player1, List.of(new RagingGoblin(), new Shock()));
        harness.setHand(player1, List.of(new SkeletalScrying()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        cast(2, List.of(0, 1));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("X=0 exiles no cards, draws no cards, and causes no life loss")
    void zeroDoesNothing() {
        harness.setHand(player1, List.of(new SkeletalScrying()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        cast(0, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The number of cards exiled must match the announced X")
    void exileCountMustMatchAnnouncedX() {
        harness.setGraveyard(player1, List.of(new RagingGoblin()));
        harness.setHand(player1, List.of(new SkeletalScrying()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> cast(2, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
