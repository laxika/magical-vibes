package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaiLiAgents.class, Forest.class, GrizzlyBears.class})
class DaiLiAgentsTest extends BaseCardTest {

    @Test
    void entersAndEarthbendsTheSameLandTwice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new DaiLiAgents(), "{3}{B}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validIds()).contains(land.getId()).doesNotContain(opposingLand.getId());
        harness.handlePermanentChosen(player1, land.getId());

        PendingInteraction.PermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(secondChoice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isTrue();
    }

    @Test
    void entersAndEarthbendsTwoDifferentLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new DaiLiAgents(), "{3}{B}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        assertThat(firstLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isCreature(gd, firstLand)).isTrue();
        assertThat(gqs.isCreature(gd, secondLand)).isTrue();
        assertThat(gqs.isLand(gd, firstLand)).isTrue();
        assertThat(gqs.isLand(gd, secondLand)).isTrue();
    }

    @Test
    void twiceEarthbendedLandReturnsOnlyOnceAndWithoutAnimationAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new DaiLiAgents(), "{3}{B}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.getId()).isNotEqualTo(land.getId());
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(returnedLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, returnedLand)).isFalse();
    }

    @Test
    void attackCountsItselfButExcludesNoncreaturesAndOpposingCreaturesWithCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent agents = addCreatureReady(player1, new DaiLiAgents());
        Permanent opposingCreature = addCreatureReady(player2, new DaiLiAgents());
        agents.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opposingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(agents)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void attackWithoutCreaturesWithCountersDoesNotChangeLife() {
        Permanent agents = addCreatureReady(player1, new DaiLiAgents());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(agents)));
            harness.passBothPriorities();
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void attackDrainsEachOpponentAndGainsLifeForEachControlledCreatureWithCounters() {
        harness.addToBattlefield(player1, new Forest());
        Permanent agents = addCreatureReady(player1, new DaiLiAgents());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureWithoutCounter = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        firstCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        secondCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(agents)));

        creatureWithoutCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(creatureWithoutCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
