package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllOfHistoryAllAtOnce.class, GrizzlyBears.class})
class AllOfHistoryAllAtOnceTest extends BaseCardTest {

    @Test
    void timeTravelsOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.TIME, 1);
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void stormCopiesForEachSpellCastBeforeItThisTurn() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        castAllOfHistoryAllAtOnce();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
    }

    private void castAllOfHistoryAllAtOnce() {
        harness.setHand(player1, List.of(new AllOfHistoryAllAtOnce()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
    }
}
