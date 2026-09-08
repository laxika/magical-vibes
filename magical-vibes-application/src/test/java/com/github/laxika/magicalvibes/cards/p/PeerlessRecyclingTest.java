package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeerlessRecycling.class, GrizzlyBears.class, HolyDay.class})
class PeerlessRecyclingTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsOnePermanentCard() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();
        harness.castSorceryWithGift(player1, 0, List.of(first.getId()), false);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(second.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void withGiftReturnsTwoPermanentCardsAndOpponentDraws() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();
        harness.castSorceryWithGift(player1, 0, List.of(first.getId(), second.getId()), true);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void giftedSpellRequiresTwoTargets() {
        Card permanent = new GrizzlyBears();
        Card secondPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent, secondPermanent));
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(permanent.getId()), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 2 and 2 targets");
    }

    @Test
    void cannotTargetNonpermanentCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, instant.getId(), false))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
