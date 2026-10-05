package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
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

@CardUsed({NarnamRenegade.class, DruidOfTheCowl.class})
class NarnamRenegadeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter after your permanent leaves the battlefield")
    void entersWithCounterAfterYourPermanentLeaves() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castRenegade();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without a counter when no permanent left the battlefield")
    void entersWithoutCounterWithoutRevolt() {
        castRenegade();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's permanent leaving the battlefield does not satisfy revolt")
    void opponentPermanentLeavingDoesNotSatisfyRevolt() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, permanent));
        castRenegade();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple departures still give exactly one counter")
    void multipleDeparturesGiveOneCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToHand(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });

        castRenegade();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Revolt applies immediately when entering without being cast")
    void revoltAppliesToNoncastEntry() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, support));

        Permanent renegade = harness.enterBattlefieldAndReturn(player1, new NarnamRenegade());

        assertThat(renegade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A permanent leaving after entry does not add a counter retroactively")
    void laterDepartureDoesNotAddCounter() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        castRenegade();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, support));

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A departure on the previous turn does not satisfy revolt")
    void previousTurnDepartureDoesNotSatisfyRevolt() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, support));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        castRenegade();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Revolt checks departures at entry rather than when the spell is cast")
    void departureWhileSpellIsOnStackSatisfiesRevolt() {
        Permanent support = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.setHand(player1, List.of(new NarnamRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, support));
        harness.passBothPriorities();

        assertThat(findRenegade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
    private void castRenegade() {
        harness.setHand(player1, List.of(new NarnamRenegade()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findRenegade() {
        return findPermanent(player1, "Narnam Renegade");
    }
}
