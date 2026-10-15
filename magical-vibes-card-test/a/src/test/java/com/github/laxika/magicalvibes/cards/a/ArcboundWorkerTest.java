package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcboundWorker.class, CrazedGoblin.class, DarksteelGargoyle.class, DarksteelIngot.class, Oxidize.class})
class ArcboundWorkerTest extends BaseCardTest {

    @Test
    void entersWithOnePlusOneCounter() {
        harness.setHand(player1, List.of(new ArcboundWorker()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent worker = findPermanent(player1, "Arcbound Worker");
        assertThat(worker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularMayPutItsCounterOnTargetArtifactCreatureWhenItDies() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent artifactCreature = addCreatureReady(player1, new DarksteelGargoyle());

        destroyWorker(worker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void modularCannotTargetNonArtifactCreature() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent nonArtifactCreature = addCreatureReady(player1, new CrazedGoblin());
        Permanent artifactCreature = addCreatureReady(player1, new DarksteelGargoyle());

        destroyWorker(worker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(artifactCreature.getId())
                .doesNotContain(nonArtifactCreature.getId());

        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularPutsAllItsCountersOnAnOpponentArtifactCreature() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent opponentArtifactCreature = addCreatureReady(player2, new DarksteelGargoyle());

        destroyWorker(worker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opponentArtifactCreature.getId());

        harness.handlePermanentChosen(player1, opponentArtifactCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponentArtifactCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void modularCannotTargetANonCreatureArtifact() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyWorker(worker);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(gargoyle.getId())
                .doesNotContain(ingot.getId(), worker.getId());

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ingot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularAddsToExistingCountersWithoutCopyingOtherCounterTypes() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        worker.setCounterCount(CounterType.CHARGE, 2);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());
        gargoyle.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        destroyWorker(worker);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gargoyle.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertInGraveyard(player1, "Arcbound Worker");
    }

    @Test
    void modularDoesNotRemainOnTheStackWithoutALegalTarget() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent goblin = addCreatureReady(player1, new CrazedGoblin());
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());

        destroyWorker(worker);

        harness.assertInGraveyard(player1, "Arcbound Worker");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ingot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void modularResolutionPromptDescribesPlusOnePlusOneCounters() {
        Permanent worker = addCreatureReady(player1, new ArcboundWorker());
        worker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent gargoyle = addCreatureReady(player1, new DarksteelGargoyle());

        destroyWorker(worker);

        harness.handlePermanentChosen(player1, gargoyle.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice.description()).contains("+1/+1").doesNotContain("-1/-1");

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gargoyle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void destroyWorker(Permanent worker) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Oxidize()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, worker.getId());
    }
}
