package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelclawsHeist.class, Forest.class, LlanowarElves.class})
class CruelclawsHeistTest extends BaseCardTest {

    @Test
    void exilesANonlandCardWithoutGrantingCastPermissionWhenGiftIsNotPromised() {
        HandSetup setup = castAndChoose(false);
        Card stolenCard = setup.stolenCard();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stolenCard);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(stolenCard.getId());
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId)
                .containsExactly(setup.remainingLand().getId());
    }

    @Test
    void giftedCardMayBeCastWithManaOfAnyType() {
        HandSetup setup = castAndChoose(true);
        Card stolenCard = setup.stolenCard();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(stolenCard);
        assertThat(gd.exilePlayPermissions.get(stolenCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, stolenCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(stolenCard);
    }

    @Test
    void cannotCastExiledCardWhenGiftWasNotPromised() {
        Card stolenCard = castAndChoose(false).stolenCard();

        assertThatThrownBy(() -> harness.castFromExile(player1, stolenCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private HandSetup castAndChoose(boolean giftPromised) {
        Card stolenCard = new LlanowarElves();
        Card remainingLand = new Forest();
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(stolenCard, remainingLand));
        harness.setHand(player1, List.of(new CruelclawsHeist()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithGift(player1, 0, player2.getId(), giftPromised);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(stolenCard.getId()));
        return new HandSetup(stolenCard, remainingLand);
    }

    private record HandSetup(Card stolenCard, Card remainingLand) {
    }
}
