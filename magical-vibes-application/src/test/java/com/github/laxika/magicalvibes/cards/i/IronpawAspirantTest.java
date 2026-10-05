package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Abrade;
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

@CardUsed({IronpawAspirant.class, GrizzlyBears.class, Abrade.class})
class IronpawAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on the target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new IronpawAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB can target a creature you control")
    void etbCanTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new IronpawAspirant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB must target itself when it is the only creature")
    void etbTargetsItselfWhenItIsTheOnlyCreature() {
        harness.castFromHand(player1, new IronpawAspirant(), "{1}{W}");
        harness.passBothPriorities();

        Permanent aspirant = findPermanent(player1, "Ironpaw Aspirant");
        harness.handlePermanentChosen(player1, aspirant.getId());
        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB can target itself even when another creature exists")
    void etbCanTargetItselfWithAnotherCreaturePresent() {
        Permanent other = harness.addToBattlefieldAndReturn(player2, new IronpawAspirant());
        harness.castFromHand(player1, new IronpawAspirant(), "{1}{W}");
        harness.passBothPriorities();

        Permanent aspirant = findPermanent(player1, "Ironpaw Aspirant");
        harness.handlePermanentChosen(player1, aspirant.getId());
        resolveAllTriggers();

        assertThat(aspirant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB resolves after Ironpaw Aspirant leaves the battlefield")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IronpawAspirant());
        harness.castFromHand(player1, new IronpawAspirant(), "{1}{W}");
        harness.passBothPriorities();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, source.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not put a counter elsewhere when its target leaves")
    void etbDoesNothingWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronpawAspirant());
        harness.castFromHand(player1, new IronpawAspirant(), "{1}{W}");
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Ironpaw Aspirant");
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
