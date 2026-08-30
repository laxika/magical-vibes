package com.github.laxika.magicalvibes.cards.k;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KingNarfisBetrayalTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I mills each player and exiles up to one matching card from each graveyard")
    void chapterIMillsAndExilesSelectedCardsFromEachGraveyard() {
        Card firstOwnCreature = new GrizzlyBears();
        Card secondOwnCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card ownNonmatch = new Shock();
        Card opponentNonmatch = new Shock();
        List<Card> ownMilledCards = List.of(new Forest(), new Forest(), new Forest(), new Forest());
        List<Card> opponentMilledCards = List.of(new Forest(), new Forest(), new Forest(), new Forest());

        harness.setGraveyard(player1, new ArrayList<>(List.of(firstOwnCreature, secondOwnCreature, ownNonmatch)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCreature, opponentNonmatch)));
        harness.setLibrary(player1, ownMilledCards);
        harness.setLibrary(player2, opponentMilledCards);
        Permanent saga = addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(firstOwnCreature.getId(), opponentCreature.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsAll(ownMilledCards)
                .doesNotContain(firstOwnCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsAll(opponentMilledCards)
                .doesNotContain(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondOwnCreature, ownNonmatch);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentNonmatch);
        assertThat(gd.findExiledCard(firstOwnCreature.getId()).sourcePermanentId()).isEqualTo(saga.getId());
        assertThat(gd.findExiledCard(opponentCreature.getId()).sourcePermanentId()).isEqualTo(saga.getId());
        assertThat(gd.findExiledCard(ownNonmatch.getId())).isNull();
    }

    @Test
    @DisplayName("Chapter I allows at most one exiled matching card from each graveyard")
    void chapterIAllowsOnlyOneCardPerGraveyard() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(firstCreature, secondCreature)));
        harness.setGraveyard(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Chapters II and III allow all earlier nonland exiled cards with any mana")
    void chaptersIIAndIIIAllowAllEarlierExiledSpellsWithAnyMana() {
        Permanent saga = addSaga(1);
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card land = new Forest();
        gd.addToExile(player1.getId(), firstCreature, saga.getId());
        gd.addToExile(player1.getId(), secondCreature, saga.getId());
        gd.addToExile(player1.getId(), land, saga.getId());
        gd.turnNumber++;

        triggerChapter();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castFromExile(player1, firstCreature.getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId())
                .contains(firstCreature.getId(), secondCreature.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chapter III's exile casting permission expires at end of turn")
    void chapterIIIExpiresAtEndOfTurn() {
        Permanent saga = addSaga(2);
        Card creature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, saga.getId());

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.exilePlayPermissions).containsEntry(creature.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = new Permanent(new KingNarfisBetrayal());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        gd.playerBattlefields.get(player1.getId()).add(saga);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
