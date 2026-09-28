package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.k.KavuScout;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Zap.class, KavuScout.class})
class ZapTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature and draws a card")
    void damagesCreatureAndDrawsCard() {
        harness.addToBattlefield(player2, new KavuScout());
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Kavu Scout"));

        assertThat(findPermanent(player2, "Kavu Scout").getMarkedDamage()).isEqualTo(1);
        harness.assertInHand(player1, "Kavu Scout");
    }

    @Test
    @DisplayName("Deals 1 damage to a player and draws a card")
    void damagesPlayerAndDrawsCard() {
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Kavu Scout");
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Deals 1 damage to a planeswalker and draws a card")
    void damagesPlaneswalkerAndDrawsCard() {
        ChandraNalaar card = new ChandraNalaar();
        card.setLoyalty(6);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new Zap()));
        harness.setLibrary(player1, List.of(new KavuScout()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInHand(player1, "Kavu Scout");
    }
}
