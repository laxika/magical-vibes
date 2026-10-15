package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AjaniCallerOfThePride;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearingSpear.class, TimberpackWolf.class, SentinelSpider.class, AjaniCallerOfThePride.class})
class SearingSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Searing Spear deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Searing Spear kills a creature with toughness 3 or less")
    void killsSmallCreature() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, wolf.getId());

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
    }

    @Test
    @DisplayName("Searing Spear does not kill a creature with toughness greater than 3")
    void doesNotKillLargeCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new SentinelSpider());

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertOnBattlefield(player2, "Sentinel Spider");
    }

    @Test
    @DisplayName("Searing Spear fizzles when its target leaves the battlefield")
    void fizzlesWhenTargetRemoved() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new TimberpackWolf());

        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, wolf.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Searing Spear removes three loyalty counters from a planeswalker")
    void damagesPlaneswalker() {
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniCallerOfThePride());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, ajani.getId());

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Ajani, Caller of the Pride");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Searing Spear");
    }

    @Test
    @DisplayName("Searing Spear can target its controller")
    void damagesItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Searing Spear marks three damage on a surviving creature you control")
    void damagesOwnCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new SentinelSpider());
        harness.setHand(player1, List.of(new SearingSpear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, spider.getId());

        harness.assertOnBattlefield(player1, "Sentinel Spider");
        assertThat(spider.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player1, "Searing Spear");
    }
}
