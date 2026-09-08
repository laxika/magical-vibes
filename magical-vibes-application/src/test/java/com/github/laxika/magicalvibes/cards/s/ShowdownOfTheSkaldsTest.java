package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShowdownOfTheSkalds.class, Forest.class, GrizzlyBears.class, Shock.class})
class ShowdownOfTheSkaldsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles the top four cards and grants play permission")
    void chapterIExilesTopFourCards() {
        List<Card> topCards = List.of(new Shock(), new Forest(), new Shock(), new Forest());
        harness.setLibrary(player1, topCards);
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(topCards).allSatisfy(card -> {
            assertThat(gd.findExiledCard(card.getId())).isNotNull();
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        });
    }

    @Test
    @DisplayName("Chapter II puts a counter on a creature you control when you cast a spell")
    void chapterIIPutsCounterOnCreatureYouControl() {
        addSaga(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        castCreatureToTrigger();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III has the same temporary spell-cast trigger")
    void chapterIIIPutsCounterOnCreatureYouControl() {
        addSaga(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        castCreatureToTrigger();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ShowdownOfTheSkalds());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void castCreatureToTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
