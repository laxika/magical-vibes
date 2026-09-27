package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VincentVengefulAtoner.class, GrizzlyBears.class})
class VincentVengefulAtonerTest extends BaseCardTest {

    @Test
    void putsOnlyOneCounterOnVincentWhenMultipleCreaturesDealCombatDamage() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(vincent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chaosDoesNotRepeatDamageToTheOpponentThatWasHit() {
        Permanent vincent = addCreatureReady(player1, new VincentVengefulAtoner());
        vincent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
    }
}
