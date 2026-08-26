package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CovetousUrge.class, Divination.class, GrizzlyBears.class, Swamp.class})
class CovetousUrgeTest extends BaseCardTest {

    @Test
    void choosesANonlandFromTheRevealedHandOrGraveyard() {
        Card land = new Swamp();
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new Divination();
        harness.setHand(player2, List.of(land, handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));

        castCovetousUrge();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileNonlandCardFromTargetHandOrGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())))
                .hasMessageContaining("valid nonland card");

        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(graveyardCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, handCard);
    }

    @Test
    void grantsPersistentAnyColorManaCastPermission() {
        Card exiledCard = new Divination();
        harness.setHand(player2, List.of(new Swamp()));
        harness.setGraveyard(player2, List.of(exiledCard));

        castCovetousUrge();
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        assertThat(gd.exilePlayPermissions.get(exiledCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(exiledCard.getId());

        harness.addMana(player1, ManaColor.GREEN, 3);
        gs.playCardFromExile(gd, player1, exiledCard.getId(), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(exiledCard.getId()));
    }

    @Test
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new CovetousUrge()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .hasMessageContaining("opponent");
    }

    private void castCovetousUrge() {
        harness.setHand(player1, List.of(new CovetousUrge()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
