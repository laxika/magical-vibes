package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuPrimarch.class, GrizzlyBears.class})
class KavuPrimarchTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new KavuPrimarch()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Primarch");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithFourCountersWhenKicked() {
        harness.setHand(player1, List.of(new KavuPrimarch()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Primarch");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void convokeCanPayGenericMana() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KavuPrimarch()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokeCreature.getId()));
        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Primarch");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
