package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenAethermage.class, MetallicSliver.class, GrizzlyBears.class, FugitiveWizard.class})
class VedalkenAethermageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns target Sliver to its owner's hand")
    void etbReturnsTargetSliver() {
        harness.addToBattlefield(player2, new MetallicSliver());
        UUID targetId = harness.getPermanentId(player2, "Metallic Sliver");
        harness.setHand(player1, List.of(new VedalkenAethermage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Metallic Sliver");
        harness.assertNotOnBattlefield(player2, "Metallic Sliver");
        harness.assertOnBattlefield(player1, "Vedalken Aethermage");
    }

    @Test
    @DisplayName("ETB cannot target a non-Sliver")
    void etbRejectsNonSliverTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
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
        harness.setLibrary(player1, List.of(new FugitiveWizard(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getName())
                .containsExactly("Fugitive Wizard");

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Vedalken Aethermage");
        harness.assertInHand(player1, "Fugitive Wizard");
    }
}
