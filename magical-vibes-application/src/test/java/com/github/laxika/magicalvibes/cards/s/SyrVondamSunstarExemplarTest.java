package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrVondamSunstarExemplar.class, GrizzlyBears.class, Forest.class})
class SyrVondamSunstarExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control dying puts a counter on Syr Vondam and gains you life")
    void allyCreatureDyingTriggersCounterAndLife() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        removeToGraveyard(bears);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Another creature you control being exiled triggers, but returning it to hand does not")
    void allyCreatureExiledTriggersButOtherLeavesDoNot() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent exiled = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        removeToExile(exiled);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);

        Permanent bounced = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bounced));
        harness.passBothPriorities();

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Another player's creature does not trigger Syr Vondam")
    void opponentCreatureDoesNotTrigger() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToGraveyard(opponentCreature);

        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("At four power, Syr Vondam's death trigger destroys a nonland permanent")
    void highPowerDeathDestroysTarget() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToGraveyard(vondam);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Syr Vondam's removal trigger does not fire below four power")
    void lowPowerRemovalTriggerDoesNotFire() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToGraveyard(vondam);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("At four power, exiling Syr Vondam offers an optional nonland target")
    void highPowerExileDestroysChosenNonlandPermanent() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToExile(vondam);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target).contains(land);
    }

    @Test
    @DisplayName("At four power, Syr Vondam's exile trigger may choose no target")
    void highPowerExileMayDeclineTarget() {
        Permanent vondam = harness.addToBattlefieldAndReturn(player1, new SyrVondamSunstarExemplar());
        addTwoCounters(vondam);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToExile(vondam);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void addTwoCounters(Permanent vondam) {
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        removeToGraveyard(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()));
        assertThat(vondam.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
