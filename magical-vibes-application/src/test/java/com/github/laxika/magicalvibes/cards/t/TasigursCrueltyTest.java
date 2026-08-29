package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TasigursCrueltyTest extends BaseCardTest {

    @Test
    void eachOpponentDiscardsTwoCards() {
        harness.setHand(player2, List.of(new Forest(), new Shock(), new GrizzlyBears()));
        castTasigursCruelty();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void delveExilesGraveyardCardsToPayGenericMana() {
        List<Card> graveyard = List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
    }

    private void castTasigursCruelty() {
        harness.setHand(player1, List.of(new TasigursCruelty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
