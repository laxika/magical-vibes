package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlinthoofBoar;
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

@CardUsed({BondBeetle.class, FlinthoofBoar.class, Forest.class})
class BondBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on your own creature")
    void etbPutsCounterOnOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new FlinthoofBoar());

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB can target a creature an opponent controls")
    void etbCanTargetOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new FlinthoofBoar());

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast on an empty battlefield and target itself after entering")
    void canTargetItselfAfterEntering() {
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent beetle = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, beetle.getId());
        resolveAllTriggers();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(beetle.getEffectivePower()).isEqualTo(1);
        assertThat(beetle.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Target is chosen after entry even when another creature is already present")
    void choosesTargetAfterEntering() {
        Permanent boar = harness.addToBattlefieldAndReturn(player2, new FlinthoofBoar());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boar.getId());
        resolveAllTriggers();

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter trigger resolves even if Bond Beetle leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new FlinthoofBoar());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boar.getId());
        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof BondBeetle);
        resolveAllTriggers();

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that leaves before resolution does not receive a counter")
    void targetLeavesBeforeResolution() {
        Permanent boar = harness.addToBattlefieldAndReturn(player2, new FlinthoofBoar());
        harness.setHand(player1, List.of(new BondBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boar.getId());
        gd.playerBattlefields.get(player2.getId()).remove(boar);
        resolveAllTriggers();

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
