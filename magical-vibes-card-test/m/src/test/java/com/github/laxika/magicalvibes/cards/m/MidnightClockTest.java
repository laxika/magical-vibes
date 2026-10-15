package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BrazenBorrower;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.v.VampireHexmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MidnightClock.class, Gingerbrute.class, BrazenBorrower.class, VampireHexmage.class})
class MidnightClockTest extends BaseCardTest {

    @Test
    void tapsForBlueMana() {
        Permanent clock = addClock();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(clock.isTapped()).isTrue();
    }

    @Test
    void addsAnHourCounterAtEachUpkeep() {
        Permanent clock = addClock();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
    }

    @Test
    void twelfthHourResetsHandAndGraveyardDrawsSevenAndExilesClock() {
        Permanent clock = addClock();
        clock.setCounterCount(CounterType.HOUR, 11);
        harness.setHand(player1, List.of(new Gingerbrute(), new Gingerbrute()));
        harness.setGraveyard(player1, List.of(new Gingerbrute()));
        harness.setLibrary(player1, cards(10));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(12);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clock.getCard());
    }

    @Test
    void doesNotTriggerAgainAfterTheTwelfthCounter() {
        Permanent clock = addClock();
        clock.setCounterCount(CounterType.HOUR, 12);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(clock.getCard());
    }

    @Test
    void addsAnHourCounterDuringOpponentsUpkeep() {
        Permanent clock = addClock();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
    }

    @Test
    void canPayForHourCounterWithItsOwnManaWhileTapped() {
        Permanent clock = addClock();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(clock.isTapped()).isTrue();
        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void twelfthHourStillShufflesAndDrawsAfterClockLeavesBattlefield() {
        Permanent clock = addClock();
        clock.setCounterCount(CounterType.HOUR, 11);
        harness.setHand(player1, List.of(new Gingerbrute()));
        harness.setGraveyard(player1, List.of(new Gingerbrute()));
        harness.setLibrary(player1, cards(10));
        harness.setHand(player2, List.of(new BrazenBorrower()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.castAdventure(player2, 0, clock.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(clock.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(clock.getCard());
    }

    @Test
    void twelfthHourStillResolvesAfterHourCountersAreRemoved() {
        Permanent clock = addClock();
        clock.setCounterCount(CounterType.HOUR, 11);
        harness.addToBattlefield(player2, new VampireHexmage());
        harness.setHand(player1, List.of(new Gingerbrute()));
        harness.setGraveyard(player1, List.of(new Gingerbrute()));
        harness.setLibrary(player1, cards(10));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, clock.getId());
        harness.passBothPriorities();
        assertThat(clock.getCounterCount(CounterType.HOUR)).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(clock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clock.getCard());
    }
    private Permanent addClock() {
        return harness.addToBattlefieldAndReturn(player1, new MidnightClock());
    }

    private List<Card> cards(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> (Card) new Gingerbrute())
                .toList();
    }
}
