package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DewdropCure.class, GrizzlyBears.class, HillGiant.class})
class DewdropCureTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsUpToTwoEligibleCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second, third, tooExpensive));
        cast(List.of(first.getId(), second.getId()), false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(third.getId(), tooExpensive.getId());
    }

    @Test
    void giftMakesOpponentDrawAndReturnsUpToThreeEligibleCreatures() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second, third, tooExpensive));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        cast(List.of(first.getId(), second.getId(), third.getId()), true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(tooExpensive.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void withoutGiftCannotChooseThreeTargets() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(first.getId(), second.getId(), third.getId()), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 2 targets");
    }

    private void cast(List<java.util.UUID> targetIds, boolean giftPromised) {
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithGift(player1, 0, targetIds, giftPromised);
        harness.passBothPriorities();
    }
}
