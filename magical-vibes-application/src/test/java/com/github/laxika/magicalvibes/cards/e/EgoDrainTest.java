package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FaerieNoble;
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

@CardUsed({EgoDrain.class, FaerieNoble.class, GrizzlyBears.class, Swamp.class})
class EgoDrainTest extends BaseCardTest {

    @Test
    void discardsChosenNonlandThenExilesFromHandWithoutAFaerie() {
        Card ownCard = new Swamp();
        Card discardedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EgoDrain(), ownCard));
        harness.setHand(player2, List.of(new Swamp(), discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotExileFromHandWhenControllerControlsAFaerie() {
        Card ownCard = new Swamp();
        Card discardedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new FaerieNoble());
        harness.setHand(player1, List.of(new EgoDrain(), ownCard));
        harness.setHand(player2, List.of(discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTargetOnlyAnOpponent() {
        harness.setHand(player1, List.of(new EgoDrain()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
