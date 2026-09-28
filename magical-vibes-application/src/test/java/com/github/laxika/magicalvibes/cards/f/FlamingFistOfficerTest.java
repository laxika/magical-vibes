package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamingFistOfficer.class, GrizzlyBears.class})
class FlamingFistOfficerTest extends BaseCardTest {

    @Test
    void getsCounterWhenAnotherCreatureYouControlLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(ally);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void getsOneCounterForEachAllyThatLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent firstAlly = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAlly = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(firstAlly);
        removeFromBattlefield(secondAlly);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotGetCounterWhenOpponentsCreatureLeaves() {
        Permanent officer = addCreatureReady(player1, new FlamingFistOfficer());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        removeFromBattlefield(opponentCreature);

        assertThat(officer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        resolveAllTriggers();
    }
}
