package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({OfHerbsAndStewedRabbit.class, GrizzlyBears.class})
class OfHerbsAndStewedRabbitTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I puts a counter on an optional creature target and creates a Food")
    void chapterIAddsCounterAndFood() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId()).doesNotContain(saga.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Chapters II and III draw, create Food, and count Foods for Halflings")
    void chaptersIIAndIIICreateFoodAndHalflings() {
        Permanent saga = addSagaWithLore(0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        triggerNextChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        int handSizeBeforeChapterII = gd.playerHands.get(player1.getId()).size();
        triggerNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeChapterII + 1);
        assertThat(findPermanents(player1, "Food")).hasSize(2);

        triggerNextChapter();
        harness.passBothPriorities();

        List<Permanent> halflings = findPermanents(player1, "Halfling");
        assertThat(halflings).hasSize(2);
        assertThat(halflings).allSatisfy(halfling -> {
            assertThat(halfling.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(halfling.getCard().getPower()).isEqualTo(1);
            assertThat(halfling.getCard().getToughness()).isEqualTo(1);
            assertThat(halfling.getCard().getSubtypes()).contains(CardSubtype.HALFLING);
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OfHerbsAndStewedRabbit());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
