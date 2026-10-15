package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.cards.o.OwlinShieldmage;
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

@CardUsed({HeatedDebate.class, GrizzlyBears.class, MuYanlingSkyDancer.class, Cancel.class, OwlinShieldmage.class})
class HeatedDebateTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 4 damage to target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        HeatedDebate heatedDebate = new HeatedDebate();
        harness.setHand(player1, List.of(heatedDebate));
        addHeatedDebateMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, heatedDebate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Ward cannot counter Heated Debate or require a life payment")
    void ignoresWard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OwlinShieldmage());
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Owlin Shieldmage");
        harness.assertInGraveyard(player2, "Owlin Shieldmage");
        harness.assertInGraveyard(player1, "Heated Debate");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target its controller's creature and marks exactly 4 damage")
    void damagesOwnCreatureExactlyFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OwlinShieldmage());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Owlin Shieldmage");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("A planeswalker with four loyalty dies from the damage")
    void killsPlaneswalkerWithFourLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MuYanlingSkyDancer());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new HeatedDebate()));
        addHeatedDebateMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Mu Yanling, Sky Dancer");
        harness.assertInGraveyard(player2, "Mu Yanling, Sky Dancer");
    }

    @Test
    @DisplayName("Still fails to resolve when its only target leaves the battlefield")
    void doesNotResolveWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        HeatedDebate first = new HeatedDebate();
        HeatedDebate second = new HeatedDebate();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(second));
        addHeatedDebateMana();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heated Debate");
        harness.assertInGraveyard(player2, "Heated Debate");
        assertThat(gd.stack).isEmpty();
    }

    private void addHeatedDebateMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
