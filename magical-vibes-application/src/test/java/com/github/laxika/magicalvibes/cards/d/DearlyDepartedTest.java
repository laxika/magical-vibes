package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DearlyDeparted.class, DoomedTraveler.class, AbbeyGriffin.class})
class DearlyDepartedTest extends BaseCardTest {

    @Test
    @DisplayName("Human creature enters with +1/+1 counter when Dearly Departed is in graveyard")
    void humanGetsCounterWhenInGraveyard() {
        // Put Dearly Departed in graveyard
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));

        // Cast a Human creature
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Doomed Traveler is 1/1 + 1 counter = 2/2
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Human creature does not get a counter")
    void nonHumanDoesNotGetCounter() {
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));

        // Cast a non-Human creature (Griffin)
        harness.setHand(player1, List.of(new AbbeyGriffin()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No counter when Dearly Departed is on the battlefield instead of graveyard")
    void noCounterWhenOnBattlefield() {
        harness.addToBattlefield(player1, new DearlyDeparted());

        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // The Human should NOT have a counter (Dearly Departed is on battlefield, not in graveyard)
        Permanent vanguard = findPermanent(player1, "Doomed Traveler");
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple Dearly Departed in graveyard grant multiple counters")
    void multipleInGraveyardGrantMultipleCounters() {
        harness.setGraveyard(player1, List.of(new DearlyDeparted(), new DearlyDeparted()));

        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        // Doomed Traveler is 1/1 + 2 counters = 3/3
        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's Human does not get a counter from your Dearly Departed")
    void opponentHumanDoesNotBenefit() {
        // Player 1 has Dearly Departed in graveyard
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));

        // Player 2 casts a Human creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DoomedTraveler()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent vanguard = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exiling Dearly Departed before the Human resolves prevents the counter")
    void graveyardConditionIsCheckedWhenCreatureEnters() {
        DearlyDeparted departed = new DearlyDeparted();
        harness.setGraveyard(player1, List.of(departed));
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(departed));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Doomed Traveler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dearly Departed reaching the graveyard before resolution grants the counter")
    void graveyardConditionNeedNotHoldWhenCreatureIsCast() {
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Doomed Traveler")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reaching the graveyard does not add counters to Humans already on the battlefield")
    void existingHumansDoNotReceiveCounters() {
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.setGraveyard(player1, List.of(new DearlyDeparted()));
        harness.setHand(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> travelers = findPermanents(player1, "Doomed Traveler");
        assertThat(travelers).hasSize(2);
        assertThat(travelers.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(travelers.getLast().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
