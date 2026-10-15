package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LymphSliver;
import com.github.laxika.magicalvibes.cards.w.WhipSpineDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenAethermage.class, LymphSliver.class, WhipSpineDrake.class})
class VedalkenAethermageTest extends BaseCardTest {

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.addToBattlefield(player2, new LymphSliver());
        UUID targetId = harness.getPermanentId(player2, "Lymph Sliver");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.passPriority(player2);
        harness.castCreature(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("ETB returns target Sliver to its owner's hand")
    void etbReturnsTargetSliver() {
        harness.addToBattlefield(player2, new LymphSliver());
        UUID targetId = harness.getPermanentId(player2, "Lymph Sliver");
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Lymph Sliver");
        harness.assertNotOnBattlefield(player2, "Lymph Sliver");
        harness.assertOnBattlefield(player1, "Vedalken Aethermage");
    }

    @Test
    @DisplayName("ETB cannot target a non-Sliver")
    void etbRejectsNonSliverTarget() {
        harness.addToBattlefield(player2, new WhipSpineDrake());
        UUID targetId = harness.getPermanentId(player2, "Whip-Spine Drake");
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Sliver");
    }

    @Test
    @DisplayName("Wizardcycling searches for a Wizard and puts it into its owner's hand")
    void wizardcyclingSearchesForWizard() {
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.setLibrary(player1, List.of(new VedalkenAethermage(), new WhipSpineDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Vedalken Aethermage");

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Vedalken Aethermage");
        harness.assertInHand(player1, "Vedalken Aethermage");
    }

    @Test
    void canEnterWithoutAnySliver() {
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vedalken Aethermage");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbReturnsYourOwnSliver() {
        harness.addToBattlefield(player1, new LymphSliver());
        UUID targetId = harness.getPermanentId(player1, "Lymph Sliver");
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lymph Sliver");
        harness.assertNotOnBattlefield(player1, "Lymph Sliver");
    }

    @Test
    void wizardcyclingDiscardsAsCostAndMayFailToFind() {
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.setLibrary(player1, List.of(new VedalkenAethermage(), new WhipSpineDrake()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Vedalken Aethermage");
        harness.assertNotInHand(player1, "Vedalken Aethermage");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Vedalken Aethermage");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void wizardcyclingCannotBeActivatedWithOnlyTwoMana() {
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Vedalken Aethermage");
        harness.assertNotInGraveyard(player1, "Vedalken Aethermage");
        assertThat(gd.stack).isEmpty();
    }
}
