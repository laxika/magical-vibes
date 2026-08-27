package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VojaJawsOfTheConclave.class, HowlpackWolf.class, GrizzlyBears.class, LlanowarElves.class})
class VojaJawsOfTheConclaveTest extends BaseCardTest {

    @Test
    void attackingPutsElfCountCountersOnEachCreatureAndDrawsForEachWolf() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        Permanent elf = addCreatureReady(player1, new LlanowarElves());
        addCreatureReady(player1, new LlanowarElves());
        Permanent wolf = addCreatureReady(player1, new HowlpackWolf());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentElf = addCreatureReady(player2, new LlanowarElves());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(voja.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentElf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    void doesNotTriggerWhenAnotherCreatureAttacks() {
        Permanent voja = addCreatureReady(player1, new VojaJawsOfTheConclave());
        addCreatureReady(player1, new LlanowarElves());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(voja.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }
}
