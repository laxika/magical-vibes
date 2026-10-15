package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({PeerlessRecycling.class, GrizzlyBears.class, HolyDay.class, Forest.class})
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

    @Test
    void withoutGiftCannotTargetTwoCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(first.getId(), second.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(permanent));
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, permanent.getId(), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void giftedSpellReturnsRemainingLegalTargetAndStillGivesGift() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();
        harness.castSorceryWithGift(player1, 0, List.of(first.getId(), second.getId()), true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setGraveyard(player1, List.of(second));
        gd.playerHands.get(player1.getId()).add(first);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsOnly(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(second.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void giftedSpellDoesNotGiveGiftWhenAllTargetsBecomeIllegal() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();
        harness.castSorceryWithGift(player1, 0, List.of(first.getId(), second.getId()), true);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setGraveyard(player1, List.of());
        gd.playerHands.get(player1.getId()).addAll(List.of(first, second));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        harness.assertInGraveyard(player1, "Peerless Recycling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canReturnLandCardWithoutGift() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new PeerlessRecycling()));
        addMana();
        harness.castSorceryWithGift(player1, 0, List.of(land.getId()), false);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
