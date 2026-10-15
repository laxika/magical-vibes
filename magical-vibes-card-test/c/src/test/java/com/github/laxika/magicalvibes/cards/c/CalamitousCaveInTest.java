package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalamitousCaveIn.class, CavernousMaw.class, GiantSpider.class, GrizzlyBears.class, ChandraNalaar.class})
class CalamitousCaveInTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to controlled and graveyard Caves to creatures and planeswalkers")
    void dealsDamageBasedOnCaves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new CavernousMaw());
        harness.addToBattlefield(player2, new CavernousMaw());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setGraveyard(player1, List.of(new CavernousMaw(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new CalamitousCaveIn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("No Caves means no damage to creatures or planeswalkers")
    void dealsNoDamageWithoutCaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new CalamitousCaveIn()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's battlefield and graveyard Caves do not contribute to damage")
    void ignoresOpponentsCaves() {
        harness.addToBattlefield(player2, new CavernousMaw());
        harness.setGraveyard(player2, List.of(new CavernousMaw(), new CavernousMaw()));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CalamitousCaveIn()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Caves are counted on resolution, including graveyard-only Caves")
    void countsCavesAtResolution() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CalamitousCaveIn()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);

        harness.setGraveyard(player1, List.of(new CavernousMaw(), new CavernousMaw(), new CavernousMaw()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(spider);
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage removes creatures and planeswalkers on both sides")
    void damagesBothPlayersPlaneswalkersAndCreatures() {
        harness.setGraveyard(player1, List.of(new CavernousMaw(), new CavernousMaw()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent ownChandra = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        Permanent opposingChandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        ownChandra.setCounterCount(CounterType.LOYALTY, 2);
        opposingChandra.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new CalamitousCaveIn()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Chandra Nalaar");
        harness.assertNotOnBattlefield(player2, "Chandra Nalaar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Chandra Nalaar");
    }
}
