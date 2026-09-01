package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
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

@CardUsed({TorchBreath.class, AirElemental.class, Cancel.class, GrizzlyBears.class, MuYanlingSkyDancer.class})
class TorchBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Costs only red mana and deals X damage to a blue creature")
    void reducesCostForBlueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new TorchBreath()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 2, List.of(target.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires the full cost for a nonblue creature")
    void requiresFullCostForNonblueCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TorchBreath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantForX(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals X damage to a blue planeswalker")
    void damagesBluePlaneswalker() {
        Permanent planeswalker = new Permanent(new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        harness.setHand(player1, List.of(new TorchBreath()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantForX(player1, 0, 2, List.of(planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TorchBreath torchBreath = new TorchBreath();
        harness.setHand(player1, List.of(torchBreath));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstantForX(player1, 0, 2, List.of(target.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, torchBreath.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new TorchBreath()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
