package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TheAesirEscapeValhalla.class, GrizzlyBears.class, Shock.class})
class TheAesirEscapeValhallaTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II use the exiled permanent's mana value")
    void chaptersUseExiledPermanentManaValue() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        Shock instant = new Shock();
        harness.setGraveyard(player1, List.of(exiledPermanent, instant));
        gd.playerLifeTotals.put(player1.getId(), 20);
        addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice.validCardIds()).containsExactly(exiledPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiledPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter III returns the Saga and its exiled card to hand")
    void chapterIIIReturnsSagaAndExiledCardToHand() {
        GrizzlyBears exiledPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledPermanent));
        Permanent saga = addSagaWithLore(0);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(exiledPermanent.getId()));
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(saga.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(saga.getCard(), exiledPermanent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheAesirEscapeValhalla());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
