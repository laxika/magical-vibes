package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KingNarfisBetrayal.class, Forest.class, GrizzlyBears.class, Shock.class, TyvarKell.class})
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

    @Test
    void chapterICanExileNewlyMilledPlaneswalker() {
        Card planeswalker = new TyvarKell();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(planeswalker, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent saga = addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(planeswalker.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.findExiledCard(planeswalker.getId()).sourcePermanentId()).isEqualTo(saga.getId());
    }

    @Test
    void chapterICanDeclineExilingFromBothGraveyards() {
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCreature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCreature)));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5).contains(opponentCreature);
        assertThat(gd.findExiledCard(ownCreature.getId())).isNull();
        assertThat(gd.findExiledCard(opponentCreature.getId())).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIWithNoEligibleCardsStillMillsBothPlayers() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setGraveyard(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIIPermitsCastingOpponentsCardAfterSagaIsSacrificed() {
        Permanent saga = addSaga(2);
        Card creature = new GrizzlyBears();
        gd.addToExile(player2.getId(), creature, saga.getId());

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        harness.assertInGraveyard(player1, "King Narfi's Betrayal");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getId()).doesNotContain(creature.getId());
    }

    @Test
    void chapterIIPermissionRequiresNormalTimingAndPaymentAndOnlyItsOwnExiledCards() {
        Permanent saga = addSaga(1);
        Card creature = new GrizzlyBears();
        Card unrelatedCreature = new GrizzlyBears();
        gd.addToExile(player1.getId(), creature, saga.getId());
        gd.addToExile(player1.getId(), unrelatedCreature, java.util.UUID.randomUUID());

        triggerChapter();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.END_STEP);
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, unrelatedCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).contains(creature.getId());
        assertThat(gd.findExiledCard(unrelatedCreature.getId())).isNotNull();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KingNarfisBetrayal());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
