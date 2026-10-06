package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldBroker.class, GrizzlyBears.class, Shock.class, Murder.class})
class ShieldBrokerTest extends BaseCardTest {

    @Test
    void putsShieldCounterOnAndControlsTargetUntilShieldCounterIsRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShieldBroker(target);

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void shieldCounterPreventsDestructionAndControlEndsAfterward() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castShieldBroker(target);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetOwnOrCommanderCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThatThrownBy(() -> castShieldBroker(own))
                .isInstanceOf(IllegalStateException.class);

        Permanent commander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        commander.setCommander(true);
        gd.playerCommanders.put(player2.getId(), List.of(commander.getCard()));
        assertThatThrownBy(() -> castShieldBroker(commander))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controlContinuesUntilAllShieldCountersAreRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.SHIELD, 1);

        castShieldBroker(target);

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void controlContinuesAfterShieldBrokerLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castShieldBroker(target);

        Permanent broker = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ShieldBroker)
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, broker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(broker).contains(target);
        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void castShieldBroker(Permanent target) {
        harness.setHand(player1, List.of(new ShieldBroker()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();
    }
}
