package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DovinGrandArbiter;
import com.github.laxika.magicalvibes.cards.g.GatewayPlaza;
import com.github.laxika.magicalvibes.cards.t.TerritorialBoar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkarrganHellkite.class, TerritorialBoar.class, DovinGrandArbiter.class, GatewayPlaza.class})
class SkarrganHellkiteTest extends BaseCardTest {

    @Test
    void riotAddsCounterWhenChosen() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SkarrganHellkite(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent hellkite = findHellkite();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutPlusOnePlusOneCounter() {
        Permanent hellkite = addReadyHellkite();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerritorialBoar());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hellkite.isTapped()).isFalse();
    }

    @Test
    void dealsTwoDamageToOneTargetWithCounter() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerritorialBoar());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 2));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dividesTwoDamageBetweenCreatureAndPlayer() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent creatureTarget = harness.addToBattlefieldAndReturn(player2, new TerritorialBoar());
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(creatureTarget.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(creatureTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void canDealDamageToAPlaneswalker() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DovinGrandArbiter());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.addMana(player1, ManaColor.RED, 4);

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 2));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void cannotTargetAnOrdinaryLand() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GatewayPlaza());
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void riotCanGrantHasteInsteadOfCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SkarrganHellkite(), "{3}{R}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent hellkite = findHellkite();
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.HASTE)).isTrue();
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.addMana(player1, ManaColor.RED, 4);
        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterIsRequiredOnlyWhenActivating() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(player2.getId(), 2));
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(player2.getId(), 2));
        gd.playerBattlefields.get(player1.getId()).remove(hellkite);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void losingOneTargetDoesNotRedistributeItsDamage() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TerritorialBoar());
        harness.addMana(player1, ManaColor.RED, 4);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(target.getId(), 1, player2.getId(), 1));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void cannotAssignZeroDamageToAChosenTarget() {
        Permanent hellkite = addReadyHellkite();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(
                player1, 0, 0, null, Map.of(player1.getId(), 0, player2.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new SkarrganHellkite());
        hellkite.setSummoningSick(true);
        hellkite.tap();
        hellkite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.RED, 8);
        int lifeBefore = gd.getLife(player2.getId());

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(player2.getId(), 2));
        harness.passBothPriorities();
        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(player2.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
        assertThat(hellkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyHellkite() {
        Permanent hellkite = harness.addToBattlefieldAndReturn(player1, new SkarrganHellkite());
        hellkite.setSummoningSick(false);
        return hellkite;
    }

    private Permanent findHellkite() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SkarrganHellkite)
                .findFirst()
                .orElseThrow();
    }
}
