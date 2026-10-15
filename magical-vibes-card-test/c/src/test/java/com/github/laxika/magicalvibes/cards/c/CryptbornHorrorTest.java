package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.cards.h.HeroesReunion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CryptbornHorror.class, AnnihilatingFire.class, HeroesReunion.class})
class CryptbornHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with +1/+1 counters equal to life lost by opponents this turn")
    void entersWithCountersEqualToOpponentsLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 5);

        harness.setHand(player1, List.of(new CryptbornHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent horror = findPermanent(player1, "Cryptborn Horror");
        assertThat(horror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not count life lost by its controller")
    void ignoresControllersLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 5);

        harness.setHand(player1, List.of(new CryptbornHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cryptborn Horror");
    }

    @Test
    @DisplayName("Counts cumulative life loss even when the opponent gains more life")
    void countsCumulativeLossDespiteLifeGain() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AnnihilatingFire(), new HeroesReunion(),
                new AnnihilatingFire(), new CryptbornHorror()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 21);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cryptborn Horror")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanent(player1, "Cryptborn Horror")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Includes life lost while the creature spell is on the stack")
    void evaluatesLifeLossWhenEntering() {
        harness.setHand(player1, List.of(new CryptbornHorror(), new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Cryptborn Horror")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets its counters when entering without being cast")
    void entersWithCountersWithoutBeingCast() {
        gd.lifeLostThisTurn.put(player2.getId(), 4);
        gd.lifeLostThisTurn.put(player1.getId(), 9);

        Permanent horror = harness.enterBattlefieldAndReturn(player1, new CryptbornHorror());

        assertThat(horror.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Cryptborn Horror");
    }

    @Test
    @DisplayName("Dies with no counters if nobody has lost life this turn")
    void diesWithoutAnyLifeLoss() {
        harness.setHand(player1, List.of(new CryptbornHorror()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cryptborn Horror");
    }
}
