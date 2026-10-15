package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RowdySnowballers.class})
class RowdySnowballersTest extends BaseCardTest {

    @Test
    void entersTapsAndStunsTargetCreatureOpponentControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RowdySnowballers());

        castRowdySnowballers(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RowdySnowballers());
        harness.addToBattlefield(player2, new RowdySnowballers());

        harness.castFromHand(player1, new RowdySnowballers(), "{2}{U}");

        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRowdySnowballers(java.util.UUID targetId) {
        harness.castFromHand(player1, new RowdySnowballers(), "{2}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }

    @Test
    void alreadyTappedCreatureStillGetsAStunCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RowdySnowballers());
        target.tap();

        castRowdySnowballers(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void stunReplacesFirstUntapAndAllowsNextUntap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RowdySnowballers());

        castRowdySnowballers(target.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetLeavingBeforeTriggerResolvesIsNotTappedOrStunned() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RowdySnowballers());

        castRowdySnowballers(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.assertOnBattlefield(player1, "Rowdy Snowballers");
    }

    @Test
    void targetBecomingControlledByYouBeforeResolutionIsIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RowdySnowballers());

        castRowdySnowballers(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        target.recordControlChange();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void canEnterWhenOpponentHasNoCreatures() {
        harness.castFromHand(player1, new RowdySnowballers(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rowdy Snowballers");
        assertThat(gd.stack).isEmpty();
    }
}
