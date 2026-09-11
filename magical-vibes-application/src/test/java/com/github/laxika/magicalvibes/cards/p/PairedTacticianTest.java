package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarriorEnKor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PairedTactician.class, WarriorEnKor.class, GrizzlyBears.class})
class PairedTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when it attacks with another Warrior")
    void putsCounterWhenAttackingWithAnotherWarrior() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new WarriorEnKor());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without another attacking Warrior")
    void doesNotTriggerWithoutAnotherAttackingWarrior() {
        Permanent tactician = addCreatureReady(player1, new PairedTactician());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(tactician.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
