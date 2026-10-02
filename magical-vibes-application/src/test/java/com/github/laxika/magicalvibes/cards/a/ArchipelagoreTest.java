package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MosscoatGoriak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Archipelagore.class, AlmightyBrushwagg.class, MosscoatGoriak.class})
class ArchipelagoreTest extends BaseCardTest {

    @Test
    @DisplayName("Mutation taps up to the number of times Archipelagore has mutated and locks those creatures")
    void mutationTapsAndLocksUpToMutationCountCreatures() {
        Permanent archipelagore = addCreatureReady(player1, new Archipelagore());
        Permanent bear = addCreatureReady(player2, new MosscoatGoriak());
        Permanent elves = addCreatureReady(player2, new AlmightyBrushwagg());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, archipelagore, List.of(archipelagore.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        resolveOneTrigger();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
        assertThat(elves.isTapped()).isFalse();

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, archipelagore, List.of(archipelagore.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId(), elves.getId()));
        resolveOneTrigger();

        assertThat(elves.isTapped()).isTrue();
        assertThat(elves.getSkipUntapCount()).isEqualTo(1);
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);

        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isTrue();
        assertThat(elves.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(bear.isTapped()).isFalse();
        assertThat(elves.isTapped()).isFalse();
    }

    private void resolveOneTrigger() {
        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mutationMayChooseNoTargets() {
        Permanent archipelagore = addCreatureReady(player1, new Archipelagore());
        Permanent creature = addCreatureReady(player2, new MosscoatGoriak());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, archipelagore, List.of(archipelagore.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveOneTrigger();

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
        assertThat(archipelagore.isTapped()).isFalse();
        assertThat(archipelagore.getSkipUntapCount()).isZero();
    }

    @Test
    void mutationCanLockAnAlreadyTappedCreatureYouControl() {
        Permanent archipelagore = addCreatureReady(player1, new Archipelagore());
        Permanent creature = addCreatureReady(player1, new MosscoatGoriak());
        creature.setTapped(true);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, archipelagore, List.of(archipelagore.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId()));
        resolveOneTrigger();

        harness.performUntapStep(player2);
        assertThat(creature.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void ordinaryCreatureCastDoesNotTriggerMutationAbility() {
        Permanent creature = addCreatureReady(player2, new MosscoatGoriak());

        harness.castFromHand(player1, new Archipelagore(), "{5}{U}{U}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Archipelagore");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
    }
}
