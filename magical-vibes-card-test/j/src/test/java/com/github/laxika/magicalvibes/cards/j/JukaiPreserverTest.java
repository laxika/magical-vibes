package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
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

@CardUsed({JukaiPreserver.class, BambooGroveArcher.class})
class JukaiPreserverTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature you control")
    void etbPutsCounterOnTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Channel puts a +1/+1 counter on each of two target creatures and discards the source")
    void channelPutsCountersOnTwoTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Jukai Preserver");
    }

    @Test
    @DisplayName("Channel can choose no targets")
    void channelCanChooseNoTargets() {
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jukai Preserver");
    }

    @Test
    @DisplayName("Channel cannot target an opponent's creature")
    void channelCannotTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jukai Preserver");
    }

    @Test
    void channelCanChooseOneTargetAndPaysDiscardBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(target.getId()));

        harness.assertNotInHand(player1, "Jukai Preserver");
        harness.assertInGraveyard(player1, "Jukai Preserver");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void channelCannotChooseTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jukai Preserver");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void channelCannotChooseThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(
                player1, 0, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jukai Preserver");
    }

    @Test
    void channelStillAffectsRemainingTargetWhenOneLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(first.getId(), second.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, first));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void channelRechecksThatTargetsAreStillControlledByActivator() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbilityWithMultiTargets(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).add(first);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void channelRequiresGreenMana() {
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jukai Preserver");
        harness.assertNotInGraveyard(player1, "Jukai Preserver");
    }

    @Test
    void channelCannotBeActivatedWithOnlyTwoMana() {
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateHandAbilityWithMultiTargets(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Jukai Preserver");
        harness.assertNotInGraveyard(player1, "Jukai Preserver");
    }

    @Test
    void etbDoesNotPutCounterOnCreatureThatChangesControllerBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of(new JukaiPreserver()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Jukai Preserver");
    }
}
