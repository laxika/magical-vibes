package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraTorchOfDefiance;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuriousReprisal.class, GiantSpider.class, GrizzlyBears.class, ChandraTorchOfDefiance.class})
class FuriousReprisalTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Deals 2 damage to each of two targets")
    void dealsDamageToCreatureAndPlayerTargets() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(spider.getId(), player2.getId()));

        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Requires exactly two targets")
    void requiresTwoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        List<UUID> singleTarget = List.of(bears.getId());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, singleTarget))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseTheSameTargetTwice() {
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseMoreThanTwoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player2.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canDamageBothPlayersIncludingItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void dealsLethalDamageToBothCreatureTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void damagesRemainingTargetWhenOneTargetLeavesBattlefield() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();
        harness.castSorcery(player1, 0, List.of(spider.getId(), player2.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(spider);
        gd.playerHands.get(player2.getId()).add(spider.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(spider.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Furious Reprisal");
    }

    @Test
    void canTargetPlaneswalkerDirectly() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraTorchOfDefiance());
        chandra.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FuriousReprisal()));
        giveMana();

        harness.castAndResolveSorcery(player1, 0, List.of(chandra.getId(), player2.getId()));

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 18);
    }
}
