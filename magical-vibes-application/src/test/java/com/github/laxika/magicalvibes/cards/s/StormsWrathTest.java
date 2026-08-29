package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormsWrath.class, GrizzlyBears.class, GiantSpider.class, ChandraNalaar.class})
class StormsWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to each creature and planeswalker, but not players")
    void dealsDamageToCreaturesAndPlaneswalkersOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent planeswalker = addPlaneswalker(player2, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castStormsWrath();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(spider.getMarkedDamage()).isEqualTo(4);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castStormsWrath() {
        harness.setHand(player1, List.of(new StormsWrath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = new Permanent(new ChandraNalaar());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
