package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AragornAndArwenWed.class, GrizzlyBears.class})
class AragornAndArwenWedTest extends BaseCardTest {

    @Test
    @DisplayName("Entering puts counters on other creatures and gains life for each one")
    void enteringCountersAndGainsLife() {
        Permanent firstOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        castAragornAndArwen();

        assertThat(firstOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("The entering creature does not count itself")
    void enteringCreatureDoesNotCountItself() {
        harness.setLife(player1, 10);
        Permanent aragornAndArwen = castAragornAndArwen();

        assertThat(aragornAndArwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Attacking puts counters on other creatures and gains life for each one")
    void attackingCountersAndGainsLife() {
        Permanent aragornAndArwen = harness.addToBattlefieldAndReturn(player1, new AragornAndArwenWed());
        aragornAndArwen.setSummoningSick(false);
        Permanent firstOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOther = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(firstOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondOther.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Opponent creatures receive no counters and contribute no life")
    void opponentCreaturesAreExcluded() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        Permanent source = castAragornAndArwen();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("Attack trigger includes creatures added before it resolves")
    void attackUsesCreaturesAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AragornAndArwenWed());
        source.setSummoningSick(false);
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(0));
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Attack trigger still resolves after its source leaves")
    void attackTriggerSurvivesSourceLeaving() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new AragornAndArwenWed());
        source.setSummoningSick(false);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 11);
    }
    private Permanent castAragornAndArwen() {
        harness.castFromHand(player1, new AragornAndArwenWed(), "{4}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AragornAndArwenWed)
                .findFirst()
                .orElseThrow();
    }
}
