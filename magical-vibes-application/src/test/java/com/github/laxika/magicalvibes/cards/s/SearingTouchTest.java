package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingTouch.class, CanopySpider.class, Counterspell.class})
class SearingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void damagesPlayer() {
        SearingTouch searingTouch = new SearingTouch();
        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(searingTouch.getId());
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void damagesCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());
        harness.setHand(player1, List.of(new SearingTouch()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .containsExactly(spider.getId());
        assertThat(spider.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying buyback returns the spell to hand as it resolves")
    void buybackReturnsToHand() {
        SearingTouch searingTouch = new SearingTouch();
        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, player2.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(searingTouch.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(searingTouch.getId());
    }

    @Test
    @DisplayName("A countered buyback spell goes to the graveyard")
    void counteredBuybackGoesToGraveyard() {
        SearingTouch searingTouch = new SearingTouch();
        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithBuyback(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, searingTouch.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(searingTouch.getId());
    }

    @Test
    @DisplayName("A buyback spell whose target becomes illegal goes to the graveyard")
    void fizzledBuybackGoesToGraveyard() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new CanopySpider());
        SearingTouch searingTouch = new SearingTouch();
        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, spider.getId());
        gd.playerBattlefields.get(player2.getId()).remove(spider);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(searingTouch.getId());
    }

    @Test
    @DisplayName("Paying buyback without enough mana rewinds the cast")
    void buybackWithoutManaRewinds() {
        SearingTouch searingTouch = new SearingTouch();
        harness.setHand(player1, List.of(searingTouch));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(searingTouch.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }
}
