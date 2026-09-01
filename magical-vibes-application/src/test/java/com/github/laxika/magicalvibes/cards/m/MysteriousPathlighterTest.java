package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChopDown;
import com.github.laxika.magicalvibes.cards.g.GiantKiller;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriousPathlighter.class, GiantKiller.class, ChopDown.class, GrizzlyBears.class})
class MysteriousPathlighterTest extends BaseCardTest {

    @Test
    void creatureWithAdventureEntersWithAnAdditionalCounterEvenWhenCastAsCreature() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());

        harness.setHand(player1, List.of(new GiantKiller()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent giantKiller = findPermanent(player1, "Giant Killer");

        assertThat(giantKiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void nonAdventureCreaturesAndOpponentsCreaturesDoNotGetAnAdditionalCounter() {
        harness.addToBattlefield(player1, new MysteriousPathlighter());

        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingGiantKiller = harness.addToBattlefieldAndReturn(player2, new GiantKiller());

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingGiantKiller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
