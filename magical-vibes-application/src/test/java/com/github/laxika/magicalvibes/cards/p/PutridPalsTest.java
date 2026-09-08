package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PutridPals.class, GrizzlyBears.class})
class PutridPalsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters after your permanent leaves the battlefield")
    void entersWithCountersAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when no permanent left the battlefield")
    void entersWithoutCountersWithoutDisappear() {
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy Disappear")
    void opponentPermanentLeavingDoesNotSatisfyDisappear() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castPutridPals();

        assertThat(findPutridPals().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castPutridPals() {
        harness.setHand(player1, List.of(new PutridPals()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findPutridPals() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Putrid Pals"))
                .findFirst().orElseThrow();
    }
}
