package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.i.ImpassionedOrator;
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

@CardUsed({HallowedPriest.class, AngelOfMercy.class, ImpassionedOrator.class})
class HallowedPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when its controller gains life")
    void getsCounterOnLifeGain() {
        harness.addToBattlefield(player1, new HallowedPriest());
        Permanent priest = findPermanent(player1, "Hallowed Priest");

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when an opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new HallowedPriest());
        Permanent priest = findPermanent(player1, "Hallowed Priest");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Priest gets one counter for each separate life-gain event")
    void eachPriestTriggersForEachLifeGainEvent() {
        harness.addToBattlefield(player1, new HallowedPriest());
        harness.addToBattlefield(player1, new HallowedPriest());
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.addToBattlefield(player1, new ImpassionedOrator());
        List<Permanent> priests = findPermanents(player1, "Hallowed Priest");
        harness.setHand(player1, List.of(new ImpassionedOrator()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        assertThat(priests).allSatisfy(priest ->
                assertThat(priest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("A Priest entering before life is gained sees that life-gain event")
    void enteringPriestSeesSubsequentLifeGain() {
        harness.addToBattlefield(player1, new ImpassionedOrator());
        harness.setHand(player1, List.of(new HallowedPriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        assertThat(findPermanent(player1, "Hallowed Priest")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
