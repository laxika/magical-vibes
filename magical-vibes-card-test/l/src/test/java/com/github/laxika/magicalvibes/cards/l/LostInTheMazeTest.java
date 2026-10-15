package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LostInTheMaze.class, GrizzlyBears.class, Island.class, Opalescence.class})
class LostInTheMazeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps exactly X creatures and stuns only creatures you do not control")
    void etbTapsXCreaturesAndStunsOpponents() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untargeted = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        castLostInTheMaze(2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownTarget.getId(), opponentTarget.getId(), untargeted.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(island.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.isTapped()).isTrue();
        assertThat(opponentTarget.isTapped()).isTrue();
        assertThat(untargeted.isTapped()).isFalse();
        assertThat(ownTarget.getCounterCount(CounterType.STUN)).isZero();
        assertThat(opponentTarget.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped creatures you control have hexproof")
    void tappedOwnCreaturesHaveHexproof() {
        harness.addToBattlefield(player1, new LostInTheMaze());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        ownCreature.tap();
        opponentCreature.tap();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HEXPROOF)).isFalse();

        ownCreature.untap();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("X=0 does not require ETB targets")
    void zeroXDoesNotRequireTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LostInTheMaze()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Already tapped opposing creatures still receive stun counters")
    void alreadyTappedCreatureReceivesStunCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        creature.setCounterCount(CounterType.STUN, 1);

        castLostInTheMaze(1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Remaining legal targets resolve when another target leaves the battlefield")
    void resolvesForRemainingTarget() {
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLostInTheMaze(2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, removed.getId());
        harness.handlePermanentChosen(player1, remaining.getId());
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(remaining.isTapped()).isTrue();
        assertThat(remaining.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lost in the Maze has hexproof while it is itself a tapped creature")
    void animatedTappedSourceHasHexproof() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent maze = harness.addToBattlefieldAndReturn(player1, new LostInTheMaze());

        assertThat(gqs.isCreature(gd, maze)).isTrue();
        assertThat(gqs.hasKeyword(gd, maze, Keyword.HEXPROOF)).isFalse();
        maze.tap();

        assertThat(gqs.hasKeyword(gd, maze, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Flash permits casting during the end step")
    void canCastDuringEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.END_STEP);

        castLostInTheMaze(1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("X greater than 100 still requires and affects exactly X creatures")
    void canTargetMoreThanOneHundredCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        }

        castLostInTheMaze(101);
        harness.passBothPriorities();
        for (Permanent creature : creatures) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> {
            assertThat(creature.isTapped()).isTrue();
            assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        });
    }

    private void castLostInTheMaze(int xValue) {
        harness.setHand(player1, List.of(new LostInTheMaze()));
        harness.addMana(player1, ManaColor.BLUE, xValue + 2);
        gs.playCard(gd, player1, 0, xValue, null, null);
    }
}
