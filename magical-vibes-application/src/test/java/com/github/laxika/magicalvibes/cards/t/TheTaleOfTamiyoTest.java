package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TheTaleOfTamiyo.class, Shock.class, Forest.class, GrizzlyBears.class,
        Divination.class, TamiyoTheMoonSage.class, ChandraNalaar.class})
class TheTaleOfTamiyoTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I through III repeat milling and draw after a shared card type")
    void chaptersRepeatMillingAndDraw() {
        Shock first = new Shock();
        Shock second = new Shock();
        Forest drawn = new Forest();
        GrizzlyBears last = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, drawn, last));
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(first, second, last);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Chapter IV targets instants, sorceries, and Tamiyo planeswalkers")
    void chapterIVFiltersGraveyards() {
        Card instant = new Shock();
        Card sorcery = new Divination();
        Card tamiyoPlaneswalker = new TamiyoTheMoonSage();
        Card otherPlaneswalker = new ChandraNalaar();
        Card land = new Forest();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, List.of(instant, sorcery, tamiyoPlaneswalker, otherPlaneswalker, land));
        harness.setGraveyard(player2, List.of(opponentInstant));
        addSagaWithLore(3);

        advanceToNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                instant.getId(), sorcery.getId(), tamiyoPlaneswalker.getId());
    }

    @Test
    @DisplayName("Chapter IV exiles a selected card and offers its copy at normal cost")
    void chapterIVCastsSelectedCopy() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        addSagaWithLore(3);

        advanceToNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTaleOfTamiyo());
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
