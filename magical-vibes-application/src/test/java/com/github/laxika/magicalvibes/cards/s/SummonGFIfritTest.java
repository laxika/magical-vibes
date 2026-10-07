package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({SummonGFIfrit.class, Forest.class})
class SummonGFIfritTest extends BaseCardTest {

    @Test
    void chapterIOptionallyDiscardsAndDraws() {
        Permanent saga = addSagaWithLore(0);
        SummonGFIfrit discardedCard = new SummonGFIfrit();
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId())
                .contains(discardedCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId())
                .contains(drawnCard.getId());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIICanBeDeclined() {
        Permanent saga = addSagaWithLore(1);
        SummonGFIfrit cardInHand = new SummonGFIfrit();
        harness.setHand(player1, List.of(cardInHand));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId())
                .containsExactly(cardInHand.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    void chapterIIIAddsRedMana() {
        Permanent saga = addSagaWithLore(2);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIVAddsRedManaThenSagaIsSacrificed() {
        Permanent saga = addSagaWithLore(3);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void enteringTriggersChapterI() {
        Forest discardedCard = new Forest();
        Forest drawnCard = new Forest();
        SummonGFIfrit summon = new SummonGFIfrit();
        harness.castFromHand(player1, summon, "{2}{R}");
        harness.setHand(player1, List.of(discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(summon);
                    assertThat(permanent.getCounterCount(CounterType.LORE)).isEqualTo(1);
                });
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterIIDoesNotDrawWithoutACardToDiscard() {
        addSagaWithLore(1);
        Forest libraryCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToNextChapter();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonGFIfrit());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
