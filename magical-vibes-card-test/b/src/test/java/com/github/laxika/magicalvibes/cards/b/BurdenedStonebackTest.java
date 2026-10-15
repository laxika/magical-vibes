package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SizzlingChangeling;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurdenedStoneback.class, SizzlingChangeling.class, Plains.class})
class BurdenedStonebackTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (effectively 2/2)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BurdenedStoneback(), "{1}{W}");
        harness.passBothPriorities();

        Permanent stoneback = findPermanent(player1, "Burdened Stoneback");

        assertThat(gd.stack).isEmpty();
        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(stoneback.getEffectivePower()).isEqualTo(2);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability grants target creature indestructible until end of turn")
    void abilityGrantsIndestructible() {
        addReadyStoneback(player1);
        harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player1, "Sizzling Changeling");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent bears = harness.getGameQueryService().findPermanentById(gd, bearsId);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Activated ability removes a -1/-1 counter from source")
    void abilityRemovesCounter() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.addToBattlefieldAndReturn(player2, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Sizzling Changeling");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Started with 2, now should have 1
        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(stoneback.getEffectivePower()).isEqualTo(3);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can activate ability twice (with two -1/-1 counters)")
    void canActivateTwiceWithTwoCounters() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.addToBattlefieldAndReturn(player1, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First activation
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID bearsId = harness.getPermanentId(player1, "Sizzling Changeling");
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        // Second activation
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(stoneback.getEffectivePower()).isEqualTo(4);
        assertThat(stoneback.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate ability when no counters remain")
    void cannotActivateWithoutCounters() {
        Permanent stoneback = addReadyStoneback(player1);
        stoneback.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        harness.addToBattlefieldAndReturn(player2, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Sizzling Changeling");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Cannot activate ability during combat (sorcery speed only)")
    void cannotActivateDuringCombat() {
        addReadyStoneback(player1);
        harness.addToBattlefieldAndReturn(player2, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Sizzling Changeling");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Cannot activate ability on opponent's turn (sorcery speed only)")
    void cannotActivateOnOpponentsTurn() {
        addReadyStoneback(player1);
        harness.addToBattlefieldAndReturn(player2, new SizzlingChangeling()).setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Sizzling Changeling");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Can target itself to gain indestructible")
    void canTargetSelf() {
        Permanent stoneback = addReadyStoneback(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, stoneback.getId());
        harness.passBothPriorities();

        assertThat(stoneback.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Indestructible expires at cleanup")
    void indestructibleExpiresAtCleanup() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, stoneback.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, stoneback, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, stoneback, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Can remove a +1/+1 counter as the activation cost")
    void canRemovePlusCounter() {
        Permanent stoneback = addReadyStoneback(player1);
        stoneback.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        stoneback.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, stoneback.getId());
        assertThat(stoneback.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, stoneback, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, stoneback, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Can remove a charge counter and activate while summoning sick")
    void canRemoveOtherCounterWhileSummoningSick() {
        Permanent stoneback = addReadyStoneback(player1);
        stoneback.setSummoningSick(true);
        stoneback.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
        stoneback.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, stoneback.getId());
        assertThat(stoneback.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, stoneback, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while a spell is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent stoneback = addReadyStoneback(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BurdenedStoneback(), "{1}{W}");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, stoneback.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent stoneback = addReadyStoneback(player1);
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters with counters when put onto the battlefield without being cast")
    void entersWithCountersWithoutBeingCast() {
        Permanent stoneback = harness.enterBattlefieldAndReturn(player1, new BurdenedStoneback());

        assertThat(stoneback.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyStoneback(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new BurdenedStoneback());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        return perm;
    }
}
