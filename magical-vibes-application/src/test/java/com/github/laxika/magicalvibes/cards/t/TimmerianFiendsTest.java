package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.j.JovensTools;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JovensTools.class, TimmerianFiends.class})
class TimmerianFiendsTest extends BaseCardTest {

    /** Fiends on player1's battlefield, three black mana available, player1's main phase. */
    private TimmerianFiends fiendsReady() {
        TimmerianFiends fiends = new TimmerianFiends();
        harness.addToBattlefield(player1, fiends);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return fiends;
    }

    private UUID opponentArtifactId() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JovensTools());
        return artifact.getId();
    }

    @Test
    @DisplayName("Anteing the top card of the library keeps the artifact and exiles that card")
    void anteingKeepsArtifact() {
        fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>(List.of(new JovensTools())));

        harness.activateAbility(player1, 0, null, artifactId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Joven's Tools");
        harness.assertOnBattlefield(player2, "Joven's Tools");
        // Sacrifice is a cost, so the Fiends are in their controller's graveyard either way.
        harness.assertInGraveyard(player1, "Timmerian Fiends");
    }

    @Test
    @DisplayName("Declining to ante swaps the cards: artifact to your graveyard, Fiends to theirs")
    void decliningExchangesCards() {
        fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>(List.of(new JovensTools())));

        harness.activateAbility(player1, 0, null, artifactId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        // The library is untouched — declining antes nothing.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Joven's Tools");
        harness.assertInGraveyard(player1, "Joven's Tools");
        harness.assertInGraveyard(player2, "Timmerian Fiends");
        harness.assertNotInGraveyard(player1, "Timmerian Fiends");
    }

    @Test
    @DisplayName("The artifact owner chooses even when another player controls the artifact")
    void artifactOwnerMakesChoice() {
        fiendsReady();
        JovensTools artifactCard = new JovensTools();
        artifactCard.setOwnerId(player2.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, artifactCard);
        gd.stolenCreatures.put(artifact.getId(), player2.getId());
        harness.setLibrary(player2, new ArrayList<>(List.of(new JovensTools())));

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(((PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Joven's Tools");
        harness.assertInGraveyard(player2, "Timmerian Fiends");
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
        harness.assertInGraveyard(player1, "Joven's Tools");
        harness.assertInGraveyard(player2, "Timmerian Fiends");
    }

    @Test
    @DisplayName("Declining to ante moves Fiends from exile to the artifact owner's graveyard")
    void decliningExchangeMovesFiendsFromExile() {
        TimmerianFiends fiends = fiendsReady();
        UUID artifactId = opponentArtifactId();
        harness.setLibrary(player2, new ArrayList<>(List.of(new JovensTools())));

        harness.activateAbility(player1, 0, null, artifactId);
        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(fiends.getId()));
        harness.setExile(player1, List.of(fiends));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Timmerian Fiends");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(fiends.getId()));
    }

    @Test
    @DisplayName("The ability cannot target a nonartifact permanent")
    void cannotTargetNonArtifact() {
        fiendsReady();
        Permanent nonArtifact = harness.addToBattlefieldAndReturn(player2, new TimmerianFiends());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target does not match the required predicate");
    }
}
