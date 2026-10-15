package com.github.laxika.magicalvibes.cards.k;

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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorvoldAndTheNobleThief.class, Forest.class, GrizzlyBears.class})
class KorvoldAndTheNobleThiefTest extends BaseCardTest {

    @Test
    void firstTwoChaptersCreateTreasureTokens() {
        Permanent saga = addSaga(0);

        triggerChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);

        saga.setCounterCount(CounterType.LORE, 1);
        triggerChapter();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void thirdChapterExilesThreeCardsAndLetsControllerPlayThemThisTurn() {
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        addSaga(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions.values())
                .containsOnly(player1.getId());
        assertThat(gd.exilePlayPermissions).hasSize(3);
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).hasSize(3);
    }

    @Test
    void enteringCreatesFirstTreasureWithoutWaitingForAnotherDrawStep() {
        harness.castFromHand(player1, new KorvoldAndTheNobleThief(), "{3}{R}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Korvold and the Noble Thief")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void thirdChapterExilesOnlyTheTopThreeCards() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        resolveThirdChapter();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(fourth);
        assertThat(gd.findExiledCard(first.getId()).ownerId()).isEqualTo(player2.getId());
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.findExiledCard(third.getId())).isNotNull();
        assertThat(gd.findExiledCard(fourth.getId())).isNull();
        harness.assertNotOnBattlefield(player1, "Korvold and the Noble Thief");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KorvoldAndTheNobleThief);
    }

    @Test
    void thirdChapterExilesAllRemainingCardsInAShortLibrary() {
        Card card = new Forest();
        harness.setLibrary(player2, List.of(card));
        resolveThirdChapter();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsOnlyKeys(card.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
    }

    @Test
    void thirdChapterWithEmptyLibraryStillCompletesAndSacrificesSaga() {
        harness.setLibrary(player2, List.of());
        resolveThirdChapter();

        assertThat(gd.exilePlayPermissions).isEmpty();
        harness.assertNotOnBattlefield(player1, "Korvold and the Noble Thief");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KorvoldAndTheNobleThief);
    }

    @Test
    void controllerCanPlayExiledLandAndCastExiledCreatureForItsNormalCost() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, creature));
        resolveThirdChapter();

        harness.castFromExile(player1, land.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void permissionExpiresButUnplayedCardsRemainExiled() {
        Card card = new Forest();
        harness.setLibrary(player2, List.of(card));
        resolveThirdChapter();

        harness.setLibrary(player2, List.of(new Forest()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, card.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No permission to play this exiled card");
    }

    @Test
    void opponentsTurnDoesNotAdvanceSaga() {
        Permanent saga = addSaga(1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void resolveThirdChapter() {
        addSaga(2);
        triggerChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KorvoldAndTheNobleThief());
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
