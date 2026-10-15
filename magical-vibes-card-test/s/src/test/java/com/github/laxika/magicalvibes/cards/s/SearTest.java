package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sear.class, GrizzlyBears.class, ChandraBoldPyromancer.class, Forest.class})
class SearTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 4 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature nonplaneswalker permanent")
    void cannotTargetNoncreatureNonplaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a creature or planeswalker");
    }

    @Test
    void dealsExactlyFourDamageToSurvivingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Sear");
    }

    @Test
    void destroysPlaneswalkerWithFourLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        harness.assertInGraveyard(player2, "Chandra, Bold Pyromancer");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDealDamageWhenTargetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sear()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Sear");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
