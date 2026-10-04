package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DyadrineSynthesisAmalgam.class, DockworkerDrone.class})
class DyadrineSynthesisAmalgamTest extends BaseCardTest {

    @Test
    void entersWithCountersEqualToManaSpent() {
        harness.setHand(player1, List.of(new DyadrineSynthesisAmalgam()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();

        Permanent dyadrine = findPermanent(player1, "Dyadrine, Synthesis Amalgam");
        assertThat(dyadrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void removesCountersFromTwoCreaturesDrawsAndCreatesRobot() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent first = addCreatureReady(player1, new DockworkerDrone());
        Permanent second = addCreatureReady(player1, new DockworkerDrone());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new DockworkerDrone();
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
        assertThat(robot.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(robot.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().getColors()).isEmpty();
    }

    @Test
    void decliningLeavesCountersAndBoardUnchanged() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent first = addCreatureReady(player1, new DockworkerDrone());
        Permanent second = addCreatureReady(player1, new DockworkerDrone());
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
        Permanent creature = addCreatureReady(player1, new DockworkerDrone());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }

    @Test
    void castingWithZeroXStillCountsColoredManaSpent() {
        harness.setHand(player1, List.of(new DyadrineSynthesisAmalgam()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dyadrine, Synthesis Amalgam")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersOnceForMultipleAttackersWithoutDyadrineAttacking() {
        Permanent dyadrine = addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent first = addCreatureReady(player1, new DockworkerDrone());
        Permanent second = addCreatureReady(player1, new DockworkerDrone());
        dyadrine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card drawn = new DockworkerDrone();
        harness.setLibrary(player1, List.of(drawn, new DockworkerDrone()));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(dyadrine.getId(), first.getId()));
        resolveAllTriggers();

        assertThat(dyadrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize + 1).contains(drawn);
        assertThat(findPermanents(player1, "Robot")).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void opponentsCreaturesCannotSupplyTheSecondCounter() {
        addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent own = addCreatureReady(player1, new DockworkerDrone());
        Permanent opposing = addCreatureReady(player2, new DockworkerDrone());
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposing.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card undrawn = new DockworkerDrone();
        harness.setLibrary(player1, List.of(undrawn));
        int startingHandSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(startingHandSize).doesNotContain(undrawn);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void doesNotTriggerWhenOpponentAttacks() {
        Permanent dyadrine = addCreatureReady(player1, new DyadrineSynthesisAmalgam());
        Permanent own = addCreatureReady(player1, new DockworkerDrone());
        addCreatureReady(player2, new DockworkerDrone());
        dyadrine.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        own.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(dyadrine.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Robot")).isEmpty();
    }
}
