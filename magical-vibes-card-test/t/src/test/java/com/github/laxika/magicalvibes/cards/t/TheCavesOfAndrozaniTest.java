package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCavesOfAndrozani.class, ClockworkDroid.class, TheFirstDoctor.class})
class TheCavesOfAndrozaniTest extends BaseCardTest {

    @Test
    void chapterIStunsUpToTwoTappedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        first.tap();
        second.tap();
        harness.castFromHand(player1, new TheCavesOfAndrozani(), "{3}{W}");

        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(first.getId(), second.getId(), player1.getId());

        harness.handlePermanentChosen(player1, first.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(second.getId(), player1.getId())
                .doesNotContain(first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @Test
    void chaptersIIAndIIIChooseOrSkipOneCounterKindPerNonSagaPermanent() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        saga.setCounterCount(CounterType.LORE, 1);
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToNextChapter();

        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.context())
                .isInstanceOf(ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice.class);
        UUID selectedPermanentId =
                ((ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice) firstChoice.context())
                        .targetId();
        harness.handleListChoice(player1, "+1/+1 counters");
        PendingInteraction.ColorChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.context())
                .isInstanceOf(ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice.class);
        UUID skippedPermanentId =
                ((ChoiceContext.AddAnotherCounterTypeOnEachNonSagaPermanentChoice) secondChoice.context())
                        .targetId();
        assertThat(skippedPermanentId).isNotEqualTo(selectedPermanentId);
        harness.handleListChoice(player1, "SKIP");

        Permanent selectedPermanent = selectedPermanentId.equals(first.getId()) ? first : second;
        Permanent skippedPermanent = skippedPermanentId.equals(first.getId()) ? first : second;
        assertThat(selectedPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(skippedPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    void chapterIVSearchesForADoctor() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        saga.setCounterCount(CounterType.LORE, 3);
        Card doctor = new TheFirstDoctor();
        harness.setLibrary(player1, List.of(doctor));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(doctor);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(doctor);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void chapterICanChooseFewerThanTwoTargets(int targetCount) {
        Permanent tapped = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        tapped.tap();
        harness.castFromHand(player1, new TheCavesOfAndrozani(), "{3}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(tapped.getId(), player1.getId())
                .doesNotContain(untapped.getId());
        if (targetCount == 1) {
            harness.handlePermanentChosen(player1, tapped.getId());
        }
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        assertThat(tapped.getCounterCount(CounterType.STUN)).isEqualTo(2 * targetCount);
        assertThat(untapped.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void chapterISkipsATargetThatUntapsBeforeResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        first.tap();
        second.tap();
        harness.castFromHand(player1, new TheCavesOfAndrozani(), "{3}{W}");

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        first.untap();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.STUN)).isZero();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void counterChaptersAddOnlyTheChosenKindAndExcludeAllSagas(int loreCount) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        Permanent otherSaga = harness.addToBattlefieldAndReturn(player2, new TheCavesOfAndrozani());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        Permanent noCounters = harness.addToBattlefieldAndReturn(player1, new ClockworkDroid());
        saga.setCounterCount(CounterType.LORE, loreCount);
        otherSaga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.STUN, 2);

        advanceToNextChapter();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("+1/+1 counters", "stun counters", "SKIP");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(otherSaga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(noCounters.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(loreCount + 1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIVFiltersNonDoctorsAndSacrificesAfterSearchCompletes() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        saga.setCounterCount(CounterType.LORE, 3);
        Card doctor = new TheFirstDoctor();
        Card nonDoctor = new ClockworkDroid();
        harness.setLibrary(player1, List.of(nonDoctor, doctor));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(doctor);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().shuffleAfterSelection()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(doctor).doesNotContain(nonDoctor);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDoctor);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIVResolvesWhenLibraryContainsNoDoctors() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        saga.setCounterCount(CounterType.LORE, 3);
        Card nonDoctor = new ClockworkDroid();
        harness.setLibrary(player1, List.of(nonDoctor));

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDoctor);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(nonDoctor);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIResolvesWithNoTappedCreatures() {
        Permanent untapped = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());

        harness.castFromHand(player1, new TheCavesOfAndrozani(), "{3}{W}");
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();

        assertThat(untapped.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof TheCavesOfAndrozani);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void counterChaptersResolveWithoutEligibleCounters(int loreCount) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheCavesOfAndrozani());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ClockworkDroid());
        saga.setCounterCount(CounterType.LORE, loreCount);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(loreCount + 1);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
