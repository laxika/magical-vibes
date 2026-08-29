package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FeldonRonomExcavatorTest extends BaseCardTest {

    @Test
    void damageExilesThatManyCardsAndLetsControllerChooseOneToPlay() {
        List<Card> topCards = List.of(new RagingGoblin(), new RagingGoblin(), new RagingGoblin());
        gd.playerDecks.get(player1.getId()).addAll(0, topCards);
        harness.addToBattlefield(player1, new FeldonRonomExcavator());
        UUID feldonId = harness.getPermanentId(player1, "Feldon, Ronom Excavator");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, feldonId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(topCards.get(0).getId(), topCards.get(1).getId());

        harness.handleMultipleCardsChosen(player1, List.of(topCards.get(1).getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCards.get(1).getId(), player1.getId())
                .doesNotContainKey(topCards.get(0).getId());
        assertThat(gd.playerDecks.get(player1.getId())).first().isEqualTo(topCards.get(2));
    }
}
