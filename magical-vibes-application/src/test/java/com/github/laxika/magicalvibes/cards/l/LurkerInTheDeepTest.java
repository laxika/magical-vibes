package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SeekNewKnowledge;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkerInTheDeep.class, Island.class, GrizzlyBears.class, SeekNewKnowledge.class})
class LurkerInTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Entering seeks a nonland card and manifests a conjured duplicate")
    void enteringSeeksAndManifestsDuplicate() {
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Island"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Attacking seeks a nonland card")
    void attackingSeeks() {
        addCreatureReady(player1, new LurkerInTheDeep());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Impending enters as an enchantment and becomes a creature after its last time counter")
    void impendingBecomesCreatureAfterLastTimeCounter() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent lurker = findPermanent(player1, "Lurker in the Deep");
        assertThat(lurker.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, lurker)).isFalse();

        lurker.setCounterCount(CounterType.TIME, 1);
        advanceToOwnEndStep();

        assertThat(lurker.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, lurker)).isTrue();
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    void seekingWithoutANonlandCardDoesNotConjureOrManifest() {
        Island island = new Island();
        harness.setLibrary(player1, List.of(island));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void impendingStillSeeksAndManifestsOnEntry() {
        LurkerInTheDeep sought = new LurkerInTheDeep();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard().getId()).isNotEqualTo(sought.getId());
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, manifested)).isTrue();
    }

    @Test
    void externalSeekManifestsEveryDuplicateEvenIfASoughtCardLeavesHand() {
        addCreatureReady(player1, new LurkerInTheDeep());
        LurkerInTheDeep first = new LurkerInTheDeep();
        LurkerInTheDeep second = new LurkerInTheDeep();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SeekNewKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).toList()).hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard().getId())
                            .isNotIn(first.getId(), second.getId());
                });
    }

    @Test
    void seekingDuringOpponentsTurnDoesNotManifestDuplicates() {
        addCreatureReady(player1, new LurkerInTheDeep());
        LurkerInTheDeep first = new LurkerInTheDeep();
        LurkerInTheDeep second = new LurkerInTheDeep();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new SeekNewKnowledge()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void impendingRemovesOneCounterOnlyAtItsControllersEndStep() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new LurkerInTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        Permanent lurker = findPermanent(player1, "Lurker in the Deep");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(lurker.getCounterCount(CounterType.TIME)).isEqualTo(3);

        advanceToOwnEndStep();

        assertThat(lurker.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, lurker)).isFalse();
    }

    @Test
    void timeCountersDoNotEnableImpendingWhenItsCostWasNotPaid() {
        Permanent lurker = addCreatureReady(player1, new LurkerInTheDeep());
        lurker.setCounterCount(CounterType.TIME, 3);

        advanceToOwnEndStep();

        assertThat(lurker.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, lurker)).isTrue();
    }
}
