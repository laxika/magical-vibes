package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.ExosuitSavior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GalvanizingSawship.class, ExosuitSavior.class})
class GalvanizingSawshipTest extends BaseCardTest {

    @Test
    void stationUsesTappedCreaturePower() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent creature = addCreatureReady(player1, new ExosuitSavior());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, battlefieldIndex(sawship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    void threeChargeCountersMakeTheSawshipAnArtifactCreatureWithFlyingAndHaste() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());

        assertThat(gqs.isCreature(gd, sawship)).isFalse();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.HASTE)).isFalse();

        sawship.setCounterCount(CounterType.CHARGE, 3);

        assertThat(gqs.isCreature(gd, sawship)).isTrue();
        assertThat(gqs.isArtifact(gd, sawship)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sawship)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sawship)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.HASTE)).isTrue();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationUsesPowerAtResolution() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());

        harness.activateAbility(player1, battlefieldIndex(sawship), null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isZero();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void summoningSickCreatureCanStationATappedSawship() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        sawship.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(sawship), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, sawship)).isFalse();
    }

    @Test
    void animatedSawshipCannotStationItself() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        sawship.setCounterCount(CounterType.CHARGE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sawship.isTapped()).isFalse();
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayStationCost() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());
        creature.tap();
        harness.addToBattlefield(player2, new ExosuitSavior());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sawship.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedOutsideAMainPhase() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void stationCannotBeActivatedWithANonemptyStack() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());
        harness.activateAbility(player1, battlefieldIndex(sawship), null, null);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(sawship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    void droppingBelowThreeCountersRemovesCreatureStatusFlyingAndHaste() {
        Permanent sawship = harness.addToBattlefieldAndReturn(player1, new GalvanizingSawship());
        sawship.setCounterCount(CounterType.CHARGE, 4);
        sawship.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.getEffectivePower(gd, sawship)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sawship)).isEqualTo(6);
        assertThat(gqs.isCreature(gd, sawship)).isTrue();

        sawship.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.isCreature(gd, sawship)).isFalse();
        assertThat(gqs.isArtifact(gd, sawship)).isTrue();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, sawship, Keyword.HASTE)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
