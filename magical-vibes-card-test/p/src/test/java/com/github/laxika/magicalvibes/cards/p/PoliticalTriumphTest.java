package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KreeSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoliticalTriumph.class, KreeSentinel.class})
class PoliticalTriumphTest extends BaseCardTest {

    @Test
    void scriesAndAddsPlanCounterWhenCreatureEnters() {
        Permanent triumph = addTriumph();
        Card topCard = new KreeSentinel();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player1, new KreeSentinel());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(triumph.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void fourthPlanCounterSacrificesDrawsAndPutsCountersOnControlledCreatures() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 3);
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new KreeSentinel());
        harness.setHand(player1, List.of());
        Card topCard = new KreeSentinel();
        harness.setLibrary(player1, List.of(topCard));
        Permanent enteringCreature = harness.enterBattlefieldAndReturn(player1, new KreeSentinel());

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(triumph.getCounterCount(CounterType.PLAN)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triumph);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentCreatureDoesNotTriggerThePlan() {
        Permanent triumph = addTriumph();

        harness.enterBattlefieldAndReturn(player2, new KreeSentinel());

        assertThat(gd.stack).isEmpty();
        assertThat(triumph.getCounterCount(CounterType.PLAN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void thirdCounterDoesNotTriggerTheReward() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 2);
        Card topCard = new KreeSentinel();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new KreeSentinel());

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(triumph.getCounterCount(CounterType.PLAN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(triumph);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void scryBottomDeterminesTheCardDrawnByTheReward() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 3);
        Card bottomedCard = new KreeSentinel();
        Card drawnCard = new PoliticalTriumph();
        harness.setLibrary(player1, List.of(bottomedCard, drawnCard));
        harness.setHand(player1, List.of());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new KreeSentinel());
        harness.enterBattlefieldAndReturn(player1, new KreeSentinel());

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        Permanent otherPlan = addTriumph();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomedCard);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherPlan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void rewardStillResolvesAfterPlanCountersAreRemoved() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 3);
        Card drawnCard = new KreeSentinel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new KreeSentinel());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        triumph.setCounterCount(CounterType.PLAN, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triumph);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rewardStillDrawsAndAddsCreatureCountersAfterThePlanLeaves() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 3);
        Card drawnCard = new KreeSentinel();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new KreeSentinel());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, triumph));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
    private Permanent addTriumph() {
        return harness.addToBattlefieldAndReturn(player1, new PoliticalTriumph());
    }
}
