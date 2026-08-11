package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoffinPurge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TombfireTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all cards with flashback from the target player's graveyard")
    void exilesAllCardsWithFlashback() {
        Card flashbackCard = new CoffinPurge();
        Card ordinaryCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(flashbackCard, ordinaryCard)));
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(flashbackCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(ordinaryCard.getId());
    }

    @Test
    @DisplayName("Leaves cards without flashback in the target player's graveyard")
    void leavesCardsWithoutFlashback() {
        Card ordinaryCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(ordinaryCard)));
        harness.setHand(player1, List.of(new Tombfire()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(ordinaryCard.getId());
    }
}
