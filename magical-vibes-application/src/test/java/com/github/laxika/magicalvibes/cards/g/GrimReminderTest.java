package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimReminder.class, RaiseTheAlarm.class, GreatFurnace.class})
class GrimReminderTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing a nonland card makes an opponent who cast it lose 6 life")
    void revealsNonlandAndPunishesOpponentWhoCastIt() {
        GrimReminder reminder = new GrimReminder();
        RaiseTheAlarm searched = new RaiseTheAlarm();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(searched, new GreatFurnace()));
        harness.castFromHand(player1, reminder, "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getId().equals(searched.getId()))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(searched.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(searched.getId()));
    }

    @Test
    @DisplayName("A library containing only lands does not cause life loss")
    void noNonlandCardCausesNoLifeLoss() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new GreatFurnace()));
        harness.castFromHand(player1, new GrimReminder(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent who cast a different spell does not lose life")
    void doesNotPunishOpponentWhoCastDifferentSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new RaiseTheAlarm(), "{1}{W}");
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new GrimReminder(), new GreatFurnace()));
        harness.castFromHand(player1, new GrimReminder(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Graveyard ability returns Grim Reminder during its controller's upkeep")
    void returnsFromGraveyardDuringUpkeep() {
        GrimReminder reminder = new GrimReminder();
        harness.setGraveyard(player1, List.of(reminder));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grim Reminder");
        harness.assertNotInGraveyard(player1, "Grim Reminder");
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated outside its controller's upkeep")
    void cannotActivateGraveyardAbilityOutsideUpkeep() {
        harness.setGraveyard(player1, List.of(new GrimReminder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated during an opponent's upkeep")
    void cannotActivateGraveyardAbilityDuringOpponentsUpkeep() {
        harness.setGraveyard(player1, List.of(new GrimReminder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
