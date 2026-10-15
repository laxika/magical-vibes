package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PincherBeetles;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SampleCollector.class, GrizzlyBears.class, PincherBeetles.class})
class SampleCollectorTest extends BaseCardTest {

    @Test
    void collectsEvidenceBeforeChoosingTheCreatureToCounter() {
        Permanent collector = addCreatureReady(player1, new SampleCollector());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Card firstEvidence = new GrizzlyBears();
        Card secondEvidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstEvidence, secondEvidence));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNull();

        harness.handleMultipleCardsChosen(player1, List.of(firstEvidence.getId(), secondEvidence.getId()));

        PendingInteraction.PermanentChoice targetChoice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).containsExactly(collector.getId(), ownCreature.getId())
                .doesNotContain(opposingCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(collector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningEvidenceLeavesTheGraveyardAndCountersUnchanged() {
        Permanent collector = addCreatureReady(player1, new SampleCollector());
        Card evidence = new SampleCollector();
        harness.setGraveyard(player1, List.of(evidence));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(collector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void insufficientEvidenceDoesNotCreateACounterAbility() {
        Permanent collector = addCreatureReady(player1, new SampleCollector());
        Card evidence = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(evidence));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(collector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(evidence);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exactlyThreeManaValueCanCounterTheAttackingCollector() {
        Permanent collector = addCreatureReady(player1, new SampleCollector());
        Card evidence = new SampleCollector();
        Card unselected = new SampleCollector();
        harness.setGraveyard(player1, List.of(evidence, unselected));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));
        harness.handlePermanentChosen(player1, collector.getId());

        assertThat(collector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(evidence);

        harness.passBothPriorities();

        assertThat(collector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void reflexiveAbilityCannotTargetACreatureWithShroud() {
        Permanent collector = addCreatureReady(player1, new SampleCollector());
        Permanent beetles = addCreatureReady(player1, new PincherBeetles());
        Card evidence = new SampleCollector();
        harness.setGraveyard(player1, List.of(evidence));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(evidence.getId()));

        PendingInteraction.PermanentChoice targetChoice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(collector.getId()).doesNotContain(beetles.getId());
    }
}
