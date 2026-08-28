package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GriffnautTracker.class, HillGiant.class, LightningBolt.class})
class GriffnautTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two target cards from a single graveyard when it enters")
    void exilesCardsFromSingleGraveyardOnEnter() {
        Card first = new HillGiant();
        Card second = new LightningBolt();
        Card untouched = new HillGiant();
        harness.setGraveyard(player2, List.of(first, second, untouched));

        castGriffnautTracker();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects selecting cards from different graveyards")
    void targetsMustShareOneGraveyard() {
        Card ownCard = new HillGiant();
        Card opponentCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castGriffnautTracker();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");
    }

    @Test
    @DisplayName("Allows choosing fewer than two cards")
    void canChooseFewerThanTwoCards() {
        Card chosen = new HillGiant();
        Card untouched = new LightningBolt();
        harness.setGraveyard(player1, List.of(chosen, untouched));

        castGriffnautTracker();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(chosen);
    }

    private void castGriffnautTracker() {
        harness.setHand(player1, List.of(new GriffnautTracker()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
