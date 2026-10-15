package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.f.FrenziedRaptor;
import com.github.laxika.magicalvibes.cards.g.GrazingWhiptail;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfTheWakeningSun.class, FrenziedRaptor.class, GrazingWhiptail.class})
class PriestOfTheWakeningSunTest extends BaseCardTest {

    

    

    @Test
    @DisplayName("Upkeep with Dinosaur in hand — accept reveals and gains 2 life")
    void upkeepWithDinosaurGainsLife() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep — trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInHand(player1, "Frenzied Raptor");
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals", "Frenzied Raptor"));
    }

    @Test
    @DisplayName("Upkeep with Dinosaur in hand — decline does not gain life")
    void upkeepWithDinosaurDeclined() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Upkeep without Dinosaur in hand — still triggers but gains no life")
    void upkeepWithoutDinosaurStillTriggers() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new PriestOfTheWakeningSun()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // opponent's upkeep — no trigger for player1

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Activated ability sacrifices Priest and searches for Dinosaur")
    void activatedAbilitySearchesDinosaur() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Put a Dinosaur in library
        harness.setLibrary(player1, List.of(new GrazingWhiptail(), new PriestOfTheWakeningSun()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Priest should be sacrificed (gone from battlefield)
        harness.assertNotOnBattlefield(player1, "Priest of the Wakening Sun");

        // Library search should be awaiting input
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName()).containsExactly("Grazing Whiptail");
    }

    @Test
    @DisplayName("Choosing a Dinosaur from library puts it into hand")
    void choosingDinosaurPutsIntoHand() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player1, List.of(new GrazingWhiptail(), new PriestOfTheWakeningSun()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the Dinosaur
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grazing Whiptail");
    }

    @Test
    @DisplayName("Activated ability requires {3}{W}{W} mana")
    void activatedAbilityRequiresMana() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        // Only add 4 mana (need 5)
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        // Should not be able to activate — not enough mana
        harness.assertOnBattlefield(player1, "Priest of the Wakening Sun");

        // Priest stays on battlefield (ability not activated)
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Priest goes to graveyard after sacrifice")
    void priestGoesToGraveyardAfterSacrifice() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player1, List.of(new FrenziedRaptor()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Priest of the Wakening Sun");
    }

    @Test
    @DisplayName("A Dinosaur acquired after the upkeep trigger can be revealed")
    void dinosaurAcquiredBeforeResolutionCanBeRevealed() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
        harness.assertInHand(player1, "Frenzied Raptor");
    }

    @Test
    @DisplayName("Losing the only Dinosaur before resolution prevents life gain")
    void dinosaurMustStillBeInHandAtResolution() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of(new FrenziedRaptor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.setHand(player1, List.of());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A restricted Dinosaur search may fail to find even with a match")
    void dinosaurSearchMayFailToFind() {
        harness.addToBattlefield(player1, new PriestOfTheWakeningSun());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FrenziedRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Priest of the Wakening Sun");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Frenzied Raptor");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
