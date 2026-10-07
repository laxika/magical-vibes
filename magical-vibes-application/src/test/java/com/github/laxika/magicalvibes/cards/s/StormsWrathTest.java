package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.ElspethSunsNemesis;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
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

@CardUsed({StormsWrath.class, GrizzlyBears.class, GiantSpider.class, ChandraNalaar.class,
        NyxbornColossus.class, ElspethSunsNemesis.class})
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
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({StormsWrath.class, NyxbornColossus.class, ElspethSunsNemesis.class})
    @DisplayName("Damages surviving enchantment creatures and planeswalkers on both sides exactly once")
    void damagesSurvivorsOnBothSides() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new NyxbornColossus());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus());
        Permanent ownPlaneswalker = harness.enterBattlefieldAndReturn(player1, new ElspethSunsNemesis());
        Permanent opposingPlaneswalker = harness.enterBattlefieldAndReturn(player2, new ElspethSunsNemesis());

        castStormsWrath();

        harness.assertOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertOnBattlefield(player2, "Nyxborn Colossus");
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Elspeth, Sun's Nemesis");
        harness.assertOnBattlefield(player2, "Elspeth, Sun's Nemesis");
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @CardUsed({StormsWrath.class, ElspethSunsNemesis.class})
    @DisplayName("Planeswalkers with four or fewer loyalty go to the graveyard")
    void killsPlaneswalkersWithLowLoyalty() {
        Permanent ownPlaneswalker = harness.enterBattlefieldAndReturn(player1, new ElspethSunsNemesis());
        Permanent opposingPlaneswalker = harness.enterBattlefieldAndReturn(player2, new ElspethSunsNemesis());
        ownPlaneswalker.setCounterCount(CounterType.LOYALTY, 4);
        opposingPlaneswalker.setCounterCount(CounterType.LOYALTY, 2);

        castStormsWrath();

        harness.assertNotOnBattlefield(player1, "Elspeth, Sun's Nemesis");
        harness.assertNotOnBattlefield(player2, "Elspeth, Sun's Nemesis");
        harness.assertInGraveyard(player1, "Elspeth, Sun's Nemesis");
        harness.assertInGraveyard(player2, "Elspeth, Sun's Nemesis");
    }

    @Test
    @CardUsed({StormsWrath.class})
    @DisplayName("Resolves without targets on an empty battlefield")
    void resolvesOnEmptyBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castStormsWrath();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Storm's Wrath");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castStormsWrath() {
        harness.setHand(player1, List.of(new StormsWrath()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
