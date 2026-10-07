package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ActionNewsCrew;
import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.f.FootNinjas;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurtleVan.class, ActionNewsCrew.class, DonatelloTurtleTechie.class, FootNinjas.class})
class TurtleVanTest extends BaseCardTest {

    @Test
    void attackTriggerOnlyTargetsCreatureThatCrewedThisTurn() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new ActionNewsCrew());
        Permanent bystander = addCreatureReady(player1, new ActionNewsCrew());

        crewVan(crewer);
        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(crewer.getId());
        assertThat(bystander.isTapped()).isFalse();
    }

    @Test
    void nonPartyCreatureGetsOnePlusOneCounter() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new ActionNewsCrew());

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void partyCreatureGetsItsPlusOneCountersDoubled() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new DonatelloTurtleTechie());
        crewer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void nonPartyCreatureDoesNotDoubleExistingCounters() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new ActionNewsCrew());
        crewer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void ninjaWithoutMutantOrTurtleTypeDoublesNewCounter() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new FootNinjas());

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void summoningSickCreatureCanCrewAndReceiveCounter() {
        addReadyVan();
        Permanent crewer = harness.addToBattlefieldAndReturn(player1, new ActionNewsCrew());

        crewVan(crewer);
        assertThat(crewer.isTapped()).isTrue();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayCrewWithAdditionalCreatureAfterMeetingRequiredPower() {
        addReadyVan();
        Permanent firstCrewer = addCreatureReady(player1, new ActionNewsCrew());
        Permanent secondCrewer = addCreatureReady(player1, new FootNinjas());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstCrewer.getId());
        PendingInteraction.PermanentChoice additionalChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(additionalChoice).isNotNull();
        assertThat(additionalChoice.validIds()).contains(secondCrewer.getId(), player1.getId());
        harness.handlePermanentChosen(player1, secondCrewer.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactlyInAnyOrder(firstCrewer.getId(), secondCrewer.getId());
        harness.handlePermanentChosen(player1, secondCrewer.getId());
        harness.passBothPriorities();

        assertThat(firstCrewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondCrewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackTriggerStillResolvesAfterVanLeavesBattlefield() {
        Permanent van = addReadyVan();
        Permanent crewer = addCreatureReady(player1, new DonatelloTurtleTechie());

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, van);
        harness.passBothPriorities();

        assertThat(crewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureThatCrewedAnotherVanIsNotAnEligibleTarget() {
        addReadyVan();
        addReadyVan();
        Permanent firstCrewer = addCreatureReady(player1, new ActionNewsCrew());
        Permanent secondCrewer = addCreatureReady(player1, new FootNinjas());

        crewVan(firstCrewer);
        harness.activateAbility(player1, 1, null, null);
        PendingInteraction.PermanentChoice crewChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(crewChoice).isNotNull();
        harness.handlePermanentChosen(player1, secondCrewer.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(1));
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(secondCrewer.getId());
        harness.handlePermanentChosen(player1, secondCrewer.getId());
        harness.passBothPriorities();

        assertThat(firstCrewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondCrewer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void returnedCrewerIsANewObjectAndDoesNotReceiveCounters() {
        addReadyVan();
        Permanent crewer = addCreatureReady(player1, new DonatelloTurtleTechie());

        crewVan(crewer);
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crewer.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, crewer);
        Permanent returned = addCreatureReady(player1, crewer.getCard());
        harness.passBothPriorities();

        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyVan() {
        return addCreatureReady(player1, new TurtleVan());
    }

    private void crewVan(Permanent crewer) {
        harness.activateAbility(player1, 0, null, null);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, crewer.getId());
        }
        PendingInteraction.PermanentChoice additionalChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        if (additionalChoice != null && additionalChoice.validPlayerIds().contains(player1.getId())) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();
    }
}
