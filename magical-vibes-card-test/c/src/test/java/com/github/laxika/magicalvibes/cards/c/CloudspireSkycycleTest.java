package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoostedSloop;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudspireSkycycle.class, BoostedSloop.class, GrizzlyBears.class})
class CloudspireSkycycleTest extends BaseCardTest {

    @Test
    void entersAndDistributesCountersAmongCreatureAndVehicle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(creature.getId(), vehicle.getId()));
        resolveAllTriggers();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(vehicle.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void entersAndPutsBothCountersOnOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCloudspireSkycycle(List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castCloudspireSkycycle(List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewAnimatesVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        vehicle.setSummoningSick(false);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void distributesCountersAmongTwoUncrewedVehicles() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(second.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.isCreature(gd, first)).isFalse();
        assertThat(gqs.isCreature(gd, second)).isFalse();
    }

    @Test
    void doesNotRedistributeCounterFromTargetThatLeavesBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(second);
        resolveAllTriggers();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(second.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void doesNotPutCountersOnTargetThatChangesController() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerBattlefields.get(player2.getId()).add(second);
        resolveAllTriggers();

        assertThat(first.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(second.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    void triggerResolvesAfterSkycycleLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(target.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Cloudspire Skycycle"));
        resolveAllTriggers();

        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void canEnterWithoutAnyOtherLegalTargets() {
        harness.castFromHand(player1, new CloudspireSkycycle(), "{2}{R}{W}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Cloudspire Skycycle");
        assertThat(findPermanent(player1, "Cloudspire Skycycle").getPlusOnePlusOneCounters()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void summoningSickCreatureCanCrewAndIsTappedAsCost() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void tappedCreatureCannotPayCrewCost() {
        harness.addToBattlefield(player1, new CloudspireSkycycle());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewAnimationEndsWithTheTurn() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    private void castCloudspireSkycycle(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new CloudspireSkycycle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
