package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheWiseMothman.class, Forest.class, GrizzlyBears.class, Millstone.class})
class TheWiseMothmanTest extends BaseCardTest {

    @Test
    void entersGivingEachPlayerARadCounter() {
        harness.setHand(player1, List.of(new TheWiseMothman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerRadCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.playerRadCounters).containsEntry(player2.getId(), 1);

    }

    @Test
    void attacksGivingEachPlayerARadCounter() {
        Permanent mothman = addCreatureReady(player1, new TheWiseMothman());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mothman)));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.playerRadCounters).containsEntry(player2.getId(), 1);
    }

    @Test
    void putsCountersOnUpToTheNumberOfNonlandCardsMilled() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TheWiseMothman());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 3, null, player1.getId());
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
