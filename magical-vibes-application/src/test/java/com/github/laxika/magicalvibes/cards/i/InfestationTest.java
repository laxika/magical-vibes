package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BlowflyInfestation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Infestation.class, BlowflyInfestation.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class InfestationTest extends BaseCardTest {

    @Test
    void entersWithBlowflyInfestationAndPutsCountersOnEveryCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent infestation = findPermanent(player1, "Infestation");
        Permanent blowfly = findPermanent(player1, "Blowfly Infestation");
        assertThat(blowfly.getCard().isToken()).isFalse();
        assertThat(infestation.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void evokePaysThreeLifeConjuresBlowflyAndSacrificesInfestation() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Infestation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Infestation");
        harness.assertInGraveyard(player1, "Infestation");
        assertThat(findPermanent(player1, "Blowfly Infestation").getCard().isToken()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
