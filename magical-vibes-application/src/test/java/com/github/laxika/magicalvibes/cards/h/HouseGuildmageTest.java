package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HouseGuildmage.class, Forest.class})
class HouseGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability keeps the target creature from untapping during its next untap step")
    void firstAbilitySkipsTargetCreatureUntap() {
        Permanent guildmage = addCreatureReady(player1, new HouseGuildmage());
        Permanent target = addCreatureReady(player2, new HouseGuildmage());

        prepareMainPhase();
        target.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("First ability can target only a creature")
    void firstAbilityCannotTargetLand() {
        addCreatureReady(player1, new HouseGuildmage());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Second ability surveils two cards")
    void secondAbilitySurveilsTwo() {
        Permanent guildmage = addCreatureReady(player1, new HouseGuildmage());
        Card topCard = new Forest();
        Card secondCard = new HouseGuildmage();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    void untapRestrictionExpiresAfterTargetsControllersNextUntap() {
        addCreatureReady(player1, new HouseGuildmage());
        Permanent target = addCreatureReady(player2, new HouseGuildmage());
        target.tap();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void firstAbilityDoesNotTapAnUntappedTarget() {
        addCreatureReady(player1, new HouseGuildmage());
        Permanent target = addCreatureReady(player2, new HouseGuildmage());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrderWithoutMovingTheThird() {
        addCreatureReady(player1, new HouseGuildmage());
        Card first = new Forest();
        Card second = new HouseGuildmage();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        activateSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        addCreatureReady(player1, new HouseGuildmage());
        Card first = new Forest();
        Card second = new HouseGuildmage();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        activateSurveil();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void surveilWithOneCardUsesOnlyThatCard() {
        addCreatureReady(player1, new HouseGuildmage());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));
        activateSurveil();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryCompletesWithoutAChoice() {
        Permanent guildmage = addCreatureReady(player1, new HouseGuildmage());
        harness.setLibrary(player1, List.of());
        activateSurveil();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void activateSurveil() {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
