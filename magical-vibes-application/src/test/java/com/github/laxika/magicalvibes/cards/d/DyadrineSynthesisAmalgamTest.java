package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DyadrineSynthesisAmalgam.class, GrizzlyBears.class})
class DyadrineSynthesisAmalgamTest extends BaseCardTest {

    @Test
    void entersWithCountersEqualToManaSpent() {
        harness.setHand(player1, List.of(new DyadrineSynthesisAmalgam()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent dyadrine = findPermanent(player1, "Dyadrine, Synthesis Amalgam");
        assertThat(dyadrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void removesCountersFromTwoCreaturesDrawsAndCreatesRobot() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(startingHandSize + 1)
                .contains(drawn);
        Permanent robot = findPermanents(player1, "Robot").getFirst();
        assertThat(robot.getCard().getPower()).isEqualTo(2);
        assertThat(robot.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void decliningLeavesCountersAndBoardUnchanged() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void doesNothingWhenFewerThanTwoCreaturesHaveCounters() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }
}
