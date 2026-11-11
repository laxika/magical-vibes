package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A chapter ability copied from a Saga card that isn't on the battlefield is a copy (CR 707.10) whichever way it
 * reaches the stack: with nothing to choose, or with a target (CR 603.3d), a graveyard card or a mode (CR 603.3c)
 * chosen as it is put there.
 */
@CardUsed({GrizzlyBears.class})
class SagaChapterCopyRecordTest extends BaseCardTest {

    private static final String GAIN_LABEL = "You gain 1 life";
    private static final String DESTROY_LABEL = "Destroy target creature";

    private static Card saga(String name, CardEffect chapterOne) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SAGA));
        card.setManaCost("{1}");
        card.addTrigger(Triggers.chapter(1), chapterOne);
        return card;
    }

    private StackEntry copiedChapterEntry(Card saga, String modeLabel) {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        SagaChapterService sagaChapterService = GameTestEngineContext.get().getBean(SagaChapterService.class);
        TriggerCollectionService collection = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        harness.inMutationScope(() -> {
            sagaChapterService.triggerCopiedSagaChapter(gd, saga, player1.getId(), 1);
            collection.processNextTriggeredModalTrigger(gd);
        });

        for (int round = 0; round < 5 && gd.interaction.isAwaitingInput(); round++) {
            List<Runnable> answers = new ArrayList<>();
            if (modeLabel != null) {
                answers.add(() -> harness.handleListChoice(player1, modeLabel));
            }
            answers.add(() -> harness.handlePermanentChosen(player1, target.getId()));
            answers.add(() -> harness.handleMultiplePermanentsChosen(player1, List.of(target.getId())));
            answers.add(() -> harness.handleMultipleCardsChosen(player1, List.of(graveyardCreature.getId())));
            answers.add(() -> harness.handleGraveyardCardChosen(player1, 0));
            boolean answered = false;
            for (Runnable answer : answers) {
                try {
                    answer.run();
                    answered = true;
                    break;
                } catch (RuntimeException | AssertionError refused) {
                    // The next kind of answer is tried.
                }
            }
            assertThat(answered).as("an answer to %s was accepted", gd.interaction.activeInteraction()).isTrue();
        }

        List<StackEntry> entries = gd.stack.stream()
                .filter(entry -> entry.getCard() != null && saga.getName().equals(entry.getCard().getName()))
                .toList();
        assertThat(entries).as("stack entries of the copied chapter").hasSize(1);
        return entries.getFirst();
    }

    @Test
    @DisplayName("A copied chapter that chooses nothing is a copy")
    void plainCopiedChapterIsACopy() {
        assertThat(copiedChapterEntry(saga("Plain Tale", new GainLifeEffect(1)), null).isCopy()).isTrue();
    }

    @Test
    @DisplayName("A copied chapter with a target is a copy")
    void targetedCopiedChapterIsACopy() {
        StackEntry entry = copiedChapterEntry(saga("Pointed Tale", new DestroyTargetPermanentEffect()), null);

        assertThat(entry.getTargetId()).isNotNull();
        assertThat(entry.isCopy()).isTrue();
    }

    @Test
    @DisplayName("A copied chapter with a graveyard target is a copy")
    void graveyardTargetedCopiedChapterIsACopy() {
        StackEntry entry = copiedChapterEntry(saga("Buried Tale", ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build()), null);

        assertThat(entry.isCopy()).isTrue();
    }

    @Test
    @DisplayName("A copied chapter whose mode is chosen as it triggers is a copy")
    void modalCopiedChapterIsACopy() {
        StackEntry entry = copiedChapterEntry(saga("Forked Tale", new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(GAIN_LABEL, new GainLifeEffect(1)),
                        new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)))))), GAIN_LABEL);

        assertThat(entry.isCopy()).isTrue();
    }

    @Test
    @DisplayName("A copied chapter whose chosen mode has a target is a copy")
    void modalTargetedCopiedChapterIsACopy() {
        StackEntry entry = copiedChapterEntry(saga("Forked Pointed Tale", new ChooseOneAtTriggerTimeEffect(
                new ChooseOneEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(DESTROY_LABEL, new DestroyTargetPermanentEffect(),
                                TargetFilters.creature()),
                        new ChooseOneEffect.ChooseOneOption(GAIN_LABEL, new GainLifeEffect(1)))))), DESTROY_LABEL);

        assertThat(entry.getTargetId()).isNotNull();
        assertThat(entry.isCopy()).isTrue();
    }
}
