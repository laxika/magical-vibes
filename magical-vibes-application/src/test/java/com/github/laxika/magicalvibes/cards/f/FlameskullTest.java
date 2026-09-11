package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Flameskull.class, Shock.class})
class FlameskullTest extends BaseCardTest {

    @Test
    @DisplayName("When Flameskull dies, it and the top card are exiled for a single play choice")
    void deathExilesFlameskullAndTopCard() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new Flameskull());
        Permanent flameskull = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card flameskullCard = flameskull.getCard();

        destroyFlameskull(flameskull);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(topCard, flameskullCard);
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(flameskullCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(flameskullCard.getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(flameskullCard.getId(), player1.getId())
                .doesNotContainKey(topCard.getId());
    }

    @Test
    @DisplayName("Choosing the library card does not grant permission to play Flameskull")
    void choosingTopCardUsesTheSinglePermission() {
        Card topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new Flameskull());
        Permanent flameskull = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card flameskullCard = flameskull.getCard();

        destroyFlameskull(flameskull);
        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.exilePlayPermissions)
                .containsEntry(topCard.getId(), player1.getId())
                .doesNotContainKey(flameskullCard.getId());
    }

    private void destroyFlameskull(Permanent flameskull) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, flameskull.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
