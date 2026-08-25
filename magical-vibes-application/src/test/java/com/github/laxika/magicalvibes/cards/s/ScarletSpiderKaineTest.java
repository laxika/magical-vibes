package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScarletSpiderKaine.class, Forest.class})
class ScarletSpiderKaineTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card puts a +1/+1 counter on Scarlet Spider")
    void discardingCardPutsCounterOnScarletSpider() {
        Permanent scarletSpider = castScarletSpiderWithCardInHand();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Declining the discard leaves Scarlet Spider without a counter")
    void decliningDiscardDoesNotPutCounterOnScarletSpider() {
        Permanent scarletSpider = castScarletSpiderWithCardInHand();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent castScarletSpiderWithCardInHand() {
        harness.setHand(player1, List.of(new ScarletSpiderKaine(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Scarlet Spider, Kaine");
    }
}
