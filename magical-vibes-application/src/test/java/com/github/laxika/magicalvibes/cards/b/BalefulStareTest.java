package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalefulStareTest extends BaseCardTest {

    private void castBalefulStare() {
        harness.setHand(player1, List.of(new BalefulStare()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws a card for each Mountain and red card in the opponent's hand")
    void drawsForEachMountainAndRedCard() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new Mountain(), new GoblinPiker(), new Forest())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalefulStare();

        // Mountain (subtype) + Goblin Piker (red) = 2 draws; Forest matches neither.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("A Mountain is counted via its land subtype")
    void countsMountainSubtype() {
        harness.setHand(player2, new ArrayList<>(List.of(new Mountain())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalefulStare();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("A red card is counted via its color")
    void countsRedCard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GoblinPiker())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalefulStare();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws nothing when the hand has no Mountains or red cards")
    void ignoresNonMatchingCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalefulStare();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Draws nothing against an empty hand")
    void emptyHandDrawsNothing() {
        harness.setHand(player2, List.of());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castBalefulStare();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new BalefulStare()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Baleful Stare goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GoblinPiker())));

        castBalefulStare();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Baleful Stare"));
    }
}
