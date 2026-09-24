package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PipsqueakRebelStrongarm.class, GrizzlyBears.class})
class PipsqueakRebelStrongarmTest extends BaseCardTest {

    @Test
    @DisplayName("Pipsqueak can't attack alone without a +1/+1 counter")
    void cannotAttackAloneWithoutCounter() {
        addCreatureReady(player1, new PipsqueakRebelStrongarm());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pipsqueak can attack alone with a +1/+1 counter")
    void canAttackAloneWithCounter() {
        Permanent pipsqueak = addCreatureReady(player1, new PipsqueakRebelStrongarm());
        pipsqueak.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Pipsqueak can attack alongside another creature without a counter")
    void canAttackWithAnotherCreatureWithoutCounter() {
        addCreatureReady(player1, new PipsqueakRebelStrongarm());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
