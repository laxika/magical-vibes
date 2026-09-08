package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnfulfilledDesires.class, IronTuskElephant.class, Forest.class})
class UnfulfilledDesiresTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} and 1 life loots: draw a card, then discard a card")
    void lootsForOneManaAndOneLife() {
        harness.addToBattlefield(player1, new UnfulfilledDesires());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        IronTuskElephant cardToDiscard = new IronTuskElephant();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(cardToDiscard));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isSameAs(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(cardToDiscard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws before discarding when the controller starts with an empty hand")
    void drawsBeforeDiscardingWithEmptyHand() {
        harness.addToBattlefield(player1, new UnfulfilledDesires());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate without mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new UnfulfilledDesires());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new IronTuskElephant()));
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
