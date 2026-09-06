package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Archipelagore.class, GrizzlyBears.class, LlanowarElves.class})
class ArchipelagoreTest extends BaseCardTest {

    @Test
    @DisplayName("Mutation taps up to the number of times Archipelagore has mutated and locks those creatures")
    void mutationTapsAndLocksUpToMutationCountCreatures() {
        Permanent archipelagore = addCreatureReady(player1, new Archipelagore());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

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
        assertThat(bear.getSkipUntapCount()).isEqualTo(2);
    }

    private void resolveOneTrigger() {
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }
}
