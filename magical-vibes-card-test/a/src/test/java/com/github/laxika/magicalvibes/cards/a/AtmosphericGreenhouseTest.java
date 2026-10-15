package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TapestryWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtmosphericGreenhouse.class, GrizzlyBears.class, TapestryWarden.class})
class AtmosphericGreenhouseTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts a +1/+1 counter on each creature its controller controls")
    void enteringPutsCountersOnControlledCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Station uses the tapped creature's power")
    void stationUsesTappedCreaturePower() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Eight charge counters make the Spacecraft an artifact creature with flying and trample")
    void eightChargeCountersUnlockAbilities() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());

        greenhouse.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, greenhouse)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isFalse();

        greenhouse.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, greenhouse)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void stationCanTapASummoningSickCreatureAndUnlockAtMoreThanEightCounters() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);
        greenhouse.setCounterCount(CounterType.CHARGE, 7);

        harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null);
        assertThat(bears.isTapped()).isTrue();
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(7);
        assertThat(gqs.isCreature(gd, greenhouse)).isFalse();
        harness.passBothPriorities();

        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(9);
        assertThat(gqs.isCreature(gd, greenhouse)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, greenhouse)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, greenhouse)).isEqualTo(4);
    }

    @Test
    void stationCannotTapItselfOrAnOpponentsCreature() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        greenhouse.setCounterCount(CounterType.CHARGE, 8);
        greenhouse.setSummoningSick(false);
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(greenhouse.isTapped()).isFalse();
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(8);
    }

    @Test
    void stationCannotUseAnAlreadyTappedCreature() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedOutsideAMainPhase() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void stationCannotBeActivatedWhileTheStackIsNotEmpty() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent entering = harness.enterBattlefieldAndReturn(player1, new AtmosphericGreenhouse());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(entering), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stationCannotBeActivatedDuringAnOpponentsTurn() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void stationUsesLastKnownPowerWhenTheTappedCreatureLeaves() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @CardUsed({AtmosphericGreenhouse.class, TapestryWarden.class})
    void tapestryWardenMakesStationUseTheTappedCreaturesToughness() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent warden = addCreatureReady(player1, new TapestryWarden());

        harness.activateAbility(player1, battlefieldIndex(greenhouse), null, null);
        harness.passBothPriorities();

        assertThat(warden.isTapped()).isTrue();
        assertThat(greenhouse.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void fallingBelowEightCountersRemovesCreatureStatusFlyingAndTrample() {
        Permanent greenhouse = harness.addToBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        greenhouse.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, greenhouse)).isTrue();

        greenhouse.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, greenhouse)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, greenhouse, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void enteringTriggerUsesCreaturesPresentAtResolutionAndSurvivesSourceRemoval() {
        Permanent greenhouse = harness.enterBattlefieldAndReturn(player1, new AtmosphericGreenhouse());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, greenhouse));

        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
