package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCavesOfAndrozani.class, GrizzlyBears.class, TheFifteenthDoctor.class})
class TheCavesOfAndrozaniTest extends BaseCardTest {

    @Test
    void chapterIStunsUpToTwoTappedCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new TheCavesOfAndrozani()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Card doctor = new TheFifteenthDoctor();
        harness.setLibrary(player1, List.of(doctor));

        advanceToNextChapter();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(doctor);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(doctor);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
