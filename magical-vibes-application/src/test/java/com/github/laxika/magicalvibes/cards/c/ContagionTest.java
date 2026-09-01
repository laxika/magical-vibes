package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Astrolabe;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
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

@CardUsed({Contagion.class, ShieldSphere.class, Astrolabe.class})
class ContagionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts both -2/-1 counters on a single target creature")
    void putsBothCountersOnOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(creature.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(2);
        assertThat(creature.getEffectivePower()).isEqualTo(-4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Distributes one -2/-1 counter on each of two target creatures")
    void distributesOneCounterEachAmongTwoTargets() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, List.of(creature1.getId(), creature2.getId()));

        assertThat(creature1.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(1);
        assertThat(creature1.getEffectivePower()).isEqualTo(-2);
        assertThat(creature1.getEffectiveToughness()).isEqualTo(5);
        assertThat(creature2.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(1);
        assertThat(creature2.getEffectivePower()).isEqualTo(-2);
        assertThat(creature2.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can be cast for 1 life and exiling a black card instead of its mana cost")
    void castsForAlternateCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion(), new Contagion()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(creature.getId()), 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creature.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot use the alternate cost by exiling a nonblack card")
    void cannotCastForAlternateCostWithNonblackCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion(), new ShieldSphere()));
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, List.of(creature.getId()), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot use the alternate cost without enough life")
    void cannotCastForAlternateCostWithoutEnoughLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion(), new Contagion()));
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, List.of(creature.getId()), 1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Astrolabe());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than two target creatures")
    void cannotTargetMoreThanTwoCreatures() {
        Permanent creature1 = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        Permanent creature3 = harness.addToBattlefieldAndReturn(player2, new ShieldSphere());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(creature1.getId(), creature2.getId(), creature3.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
