package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KelethSunmaneFamiliar.class, GrizzlyBears.class})
class KelethSunmaneFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on a commander that attacks")
    void commanderAttacks() {
        Card commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Permanent commander = addCreatureReady(player1, commanderCard);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(commander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a noncommander attacker")
    void noncommanderAttacks() {
        addCreatureReady(player1, new KelethSunmaneFamiliar());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
