package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.XathridGorgon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeepsakeGorgon.class, GrizzlyBears.class, XathridGorgon.class})
class KeepsakeGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("When Keepsake Gorgon becomes monstrous, it destroys a chosen non-Gorgon creature an opponent controls")
    void becomingMonstrousDestroysChosenNonGorgonCreature() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon);
    }

    @Test
    @DisplayName("Keepsake Gorgon's trigger is skipped when only a Gorgon is available")
    void triggerSkipsWhenOnlyGorgonIsAvailable() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent gorgon = harness.addToBattlefieldAndReturn(player2, new XathridGorgon());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(gorgon);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Monstrosity can be activated while monstrous but does nothing on resolution")
    void monstrosityCanBeActivatedAgainButOnlyResolvesOnce() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Multiple pending monstrosity activations only add one counter and trigger once")
    void multiplePendingActivationsBecomeMonstrousOnlyOnce() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A friendly non-Gorgon is not a legal target for the monstrous trigger")
    void triggerSkipsWhenOnlyFriendlyNonGorgonIsAvailable() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Monstrosity does not require tapping or haste")
    void monstrosityWorksWhileTappedAndSummoningSick() {
        Permanent keepsakeGorgon = harness.addToBattlefieldAndReturn(player1, new KeepsakeGorgon());
        keepsakeGorgon.setSummoningSick(true);
        keepsakeGorgon.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(keepsakeGorgon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing the counter does not let monstrosity add another counter")
    void removingCountersDoesNotRemoveMonstrousDesignation() {
        Permanent keepsakeGorgon = addReadyKeepsakeGorgon();
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        keepsakeGorgon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keepsakeGorgon.isMonstrous()).isTrue();
        assertThat(keepsakeGorgon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyKeepsakeGorgon() {
        Permanent keepsakeGorgon = harness.addToBattlefieldAndReturn(player1, new KeepsakeGorgon());
        keepsakeGorgon.setSummoningSick(false);
        return keepsakeGorgon;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
