package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NarnamRenegade;
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

@CardUsed({GreenwheelLiberator.class, NarnamRenegade.class})
class GreenwheelLiberatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters after your permanent leaves the battlefield")
    void entersWithCountersAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new NarnamRenegade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castLiberator();

        Permanent liberator = findLiberator();

        assertThat(liberator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enters without counters when no permanent left the battlefield")
    void entersWithoutCountersWithoutRevolt() {
        castLiberator();

        Permanent liberator = findLiberator();

        assertThat(liberator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new NarnamRenegade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castLiberator();

        Permanent liberator = findLiberator();

        assertThat(liberator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Revolt is checked on entry even when satisfied after casting")
    void permanentLeavingWhileSpellIsOnStackSatisfiesRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new NarnamRenegade());
        harness.setHand(player1, List.of(new GreenwheelLiberator()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passBothPriorities();

        assertThat(findLiberator().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple departing permanents still give exactly two counters")
    void multipleDeparturesDoNotMultiplyCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NarnamRenegade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NarnamRenegade());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToHand(gd, second);
        });

        castLiberator();

        assertThat(findLiberator().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A departure on a previous turn does not satisfy revolt")
    void previousTurnDepartureDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new NarnamRenegade());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        castLiberator();

        assertThat(findLiberator().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castLiberator() {
        harness.setHand(player1, List.of(new GreenwheelLiberator()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findLiberator() {
        return findPermanent(player1, "Greenwheel Liberator");
    }
}
