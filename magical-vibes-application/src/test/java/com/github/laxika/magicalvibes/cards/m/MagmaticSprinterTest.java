package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MagmaticSprinterTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two oil counters on a target artifact you control when it enters")
    void putsOilCountersOnTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        castSprinter();

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing two oil counters at the end step keeps it on the battlefield")
    void removesOilCountersToStayOnBattlefield() {
        Permanent sprinter = addSprinterWithOilCounters(2);

        beginEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sprinter.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprinter);
    }

    @Test
    @DisplayName("Declining to remove oil counters returns it to its owner's hand")
    void decliningOilPaymentReturnsItToHand() {
        addSprinterWithOilCounters(2);

        beginEndStep();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player1, "Magmatic Sprinter");
    }

    @Test
    @DisplayName("Returns to hand automatically when it has fewer than two oil counters")
    void insufficientOilCountersReturnItToHand() {
        addSprinterWithOilCounters(1);

        beginEndStep();

        harness.assertNotOnBattlefield(player1, "Magmatic Sprinter");
        harness.assertInHand(player1, "Magmatic Sprinter");
    }

    private void castSprinter() {
        harness.setHand(player1, List.of(new MagmaticSprinter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addSprinterWithOilCounters(int count) {
        Permanent sprinter = harness.addToBattlefieldAndReturn(player1, new MagmaticSprinter());
        sprinter.setCounterCount(CounterType.OIL, count);
        return sprinter;
    }

    private void beginEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
