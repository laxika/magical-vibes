package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangrenFirstborn.class, DarksteelGargoyle.class})
class FangrenFirstbornTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each attacking creature")
    void putsCountersOnEachAttackingCreature() {
        Permanent firstborn = addCreatureReady(player1, new FangrenFirstborn());
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent nonAttacker = addCreatureReady(player1, new DarksteelGargoyle());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(firstborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when another creature attacks without it")
    void doesNotTriggerWhenAnotherCreatureAttacksWithoutIt() {
        Permanent firstborn = addCreatureReady(player1, new FangrenFirstborn());
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(firstborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
