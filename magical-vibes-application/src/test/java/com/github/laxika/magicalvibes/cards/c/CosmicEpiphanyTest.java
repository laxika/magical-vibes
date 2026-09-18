package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmicEpiphany.class, Divination.class, Shock.class, GrizzlyBears.class})
class CosmicEpiphanyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each instant or sorcery in controller's graveyard")
    void drawsForEachInstantOrSorceryInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    @Test
    @DisplayName("Does not count instant or sorcery cards in an opponent's graveyard")
    void onlyCountsControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draws no cards when controller has no instant or sorcery cards in their graveyard")
    void drawsNoCardsWithoutInstantOrSorceryCards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        castCosmicEpiphany();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    private void castCosmicEpiphany() {
        harness.setHand(player1, List.of(new CosmicEpiphany()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
