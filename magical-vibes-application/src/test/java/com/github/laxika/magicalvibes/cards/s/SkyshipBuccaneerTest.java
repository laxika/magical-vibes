package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshipBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid draws a card when Skyship Buccaneer enters")
    void raidDrawsCard() {
        setDeck(player1, List.of(new Forest()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castSkyshipBuccaneer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Raid does not draw a card when no attack occurred")
    void noRaidDoesNotDrawCard() {
        setDeck(player1, List.of(new Forest()));
        castSkyshipBuccaneer();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    private void castSkyshipBuccaneer() {
        harness.setHand(player1, List.of(new SkyshipBuccaneer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player,
                         List<com.github.laxika.magicalvibes.model.Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
