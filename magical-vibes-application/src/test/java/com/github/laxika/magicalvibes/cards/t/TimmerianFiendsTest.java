package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimmerianFiendsTest extends BaseCardTest {

    /** Fiends on player1's battlefield, three black mana available, player1's main phase. */
    private void fiendsReady() {
        harness.addToBattlefield(player1, new TimmerianFiends());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private UUID opponentArtifactId() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        return artifact.getId();
    }

    @Test
    @DisplayName("Anteing the top card of the library keeps the artifact and exiles that card")
    void anteingKeepsArtifact() {
        fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, artifactId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Grizzly Bears");
        harness.assertOnBattlefield(player2, "Millstone");
        // Sacrifice is a cost, so the Fiends are in their controller's graveyard either way.
        harness.assertInGraveyard(player1, "Timmerian Fiends");
    }

    @Test
    @DisplayName("Declining to ante swaps the cards: artifact to your graveyard, Fiends to theirs")
    void decliningExchangesCards() {
        fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, artifactId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // The library is untouched — declining antes nothing.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player2, "Timmerian Fiends");
        harness.assertNotInGraveyard(player1, "Timmerian Fiends");
    }

    @Test
    @DisplayName("An owner with an empty library can't ante, so the exchange happens with no prompt")
    void emptyLibraryExchangesImmediately() {
        fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>());

        harness.activateAbility(player1, 0, null, artifactId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player2, "Timmerian Fiends");
    }

    @Test
    @DisplayName("The ability cannot target a nonartifact permanent")
    void cannotTargetNonArtifact() {
        fiendsReady();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }
}
