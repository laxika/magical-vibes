package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YasminKhan.class, Forest.class, ThinkTwice.class})
class YasminKhanTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the top card and lets its controller play it until their next end step")
    void exilesTopCardAndGrantsPlayPermissionUntilNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(topCard.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    @Test
    void canPlayExiledLandButCannotExceedLandPlayLimit() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId()));
        harness.castFromExile(player1, firstLand.getId());

        assertThat(gd.findExiledCard(firstLand.getId())).isNull();
        harness.assertOnBattlefield(player1, "Forest");

        yasmin.setTapped(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMultipleCardsChosen(player1, List.of(secondLand.getId())));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, secondLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(secondLand.getId())).isNotNull();
    }

    @Test
    void canCastExiledInstantOnOpponentsTurnByPayingItsManaCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card instant = new ThinkTwice();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(instant, drawnCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())));

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(instant.getId())).isNotNull();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castFromExile(player1, instant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Think Twice");
        assertThat(gd.findExiledCard(instant.getId())).isNull();
    }

    @Test
    void cannotCastExiledInstantOnceNextEndStepBegins() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card instant = new ThinkTwice();
        harness.setLibrary(player1, List.of(instant, new Forest()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.handleMultipleCardsChosen(player1, List.of(instant.getId())));
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(instant.getId())).isNotNull();
    }

    @Test
    void activationDuringEndStepAllowsLandPlayOnFollowingTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handleMultipleCardsChosen(player1, List.of(land.getId())));

        assertThatThrownBy(() -> harness.castFromExile(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void emptyLibraryStillPaysTapCostWithoutCreatingAChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities);

        assertThat(yasmin.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void summoningSicknessPreventsPayingTapCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new YasminKhan());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void abilityStillResolvesAndPermissionSurvivesWhenYasminLeavesBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent yasmin = addCreatureReady(player1, new YasminKhan());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(yasmin), null, null);
        gd.playerBattlefields.get(player1.getId()).remove(yasmin);
        harness.setGraveyard(player1, List.of(yasmin.getCard()));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Yasmin Khan");
    }
}
