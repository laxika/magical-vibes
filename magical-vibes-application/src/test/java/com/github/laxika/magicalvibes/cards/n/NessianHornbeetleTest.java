package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NessianHornbeetle.class})
class NessianHornbeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when you control another creature with power 4 or greater")
    void putsCounterWithAnotherCreatureWithPowerAtLeastFour() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not count itself as the other creature")
    void doesNotCountItself() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        beetle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when the other creature has power less than 4")
    void doesNotTriggerBelowPowerThreshold() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        addCreatureReady(player1, makeCreature("Medium Creature", 3, 3));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature does not satisfy the condition")
    void opponentCreatureDoesNotCount() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        addCreatureReady(player2, makeCreature("Large Creature", 4, 4));

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        addCreatureReady(player1, makeCreature("Large Creature", 4, 4));

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Rechecks the other creature's power when the ability resolves")
    void doesNothingWhenOtherCreatureFallsBelowFourBeforeResolution() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        Permanent other = addCreatureReady(player1, new NessianHornbeetle());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A different qualifying creature can satisfy the condition at resolution")
    void anotherQualifyingCreatureCanReplaceTheOriginal() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        Permanent original = addCreatureReady(player1, new NessianHornbeetle());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent replacement = addCreatureReady(player1, new NessianHornbeetle());
        replacement.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gaining a qualifying creature after combat begins does not create a trigger")
    void conditionMustBeMetWhenCombatBegins() {
        Permanent beetle = addCreatureReady(player1, new NessianHornbeetle());
        Permanent other = addCreatureReady(player1, new NessianHornbeetle());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(beetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card() {};
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
