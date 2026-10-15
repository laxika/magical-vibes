package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AncestralVision;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuryCharm.class, AncestralVision.class, GrizzlyBears.class, Millstone.class})
class FuryCharmTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        harness.addToBattlefield(player2, new Millstone());
        cast(0, harness.getPermanentId(player2, "Millstone"));

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    void destroyModeCannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostModeCannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Millstone());

        assertThatThrownBy(() -> cast(1, harness.getPermanentId(player2, "Millstone")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsCreatureAndGrantsTrampleUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(1, bears.getId());

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void removesTwoTimeCountersFromTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 3);
        cast(2, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void removesTwoTimeCountersFromTargetSuspendedCard() {
        AncestralVision target = suspendedCard(3);

        cast(2, target.getId());

        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getId(), 1);
    }

    @Test
    void cannotTargetExiledCardThatIsNotSuspended() {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));

        assertThatThrownBy(() -> cast(2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void counterRemovalCanTargetPermanentWithoutTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        target.setCounterCount(CounterType.CHARGE, 3);

        cast(2, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Millstone");
    }

    @Test
    void removesOnlyAvailableTimeCounterFromPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);

        cast(2, target.getId());

        assertThat(target.getCounterCount(CounterType.TIME)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void removingLastSuspendCounterQueuesOneCastingTrigger() {
        AncestralVision target = suspendedCard(1);

        cast(2, target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingBothSuspendCountersQueuesOneCastingTrigger() {
        AncestralVision target = suspendedCard(2);

        cast(2, target.getId());

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exiledCardWithNonSuspendTimeCountersIsNotLegalTarget() {
        AncestralVision target = suspendedCard(3);
        gd.exiledCardsWithNonSuspendTimeCounters.add(target.getId());

        assertThatThrownBy(() -> cast(2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private AncestralVision suspendedCard(int timeCounters) {
        AncestralVision target = new AncestralVision();
        harness.setExile(player2, List.of(target));
        gd.exiledCardTimeCounters.put(target.getId(), timeCounters);
        return target;
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FuryCharm()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
