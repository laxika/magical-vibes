package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContaminantGrafter.class, Forest.class, GrizzlyBears.class})
class ContaminantGrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferates once when one or more creatures deal combat damage")
    void proliferatesForBatchedCombatDamage() {
        harness.addToBattlefield(player1, new ContaminantGrafter());
        Permanent countered = addCreatureReady(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        countered.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(countered.getId()));

        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Corrupted end-step ability draws and may put a land from hand onto the battlefield")
    void corruptedDrawsAndPutsLandOntoBattlefield() {
        harness.addToBattlefield(player1, new ContaminantGrafter());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Corrupted ability does not trigger before an opponent has three poison counters")
    void corruptedDoesNotTriggerBelowPoisonThreshold() {
        harness.addToBattlefield(player1, new ContaminantGrafter());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
