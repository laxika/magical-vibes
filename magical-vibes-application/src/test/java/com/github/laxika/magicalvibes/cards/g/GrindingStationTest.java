package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrindingStationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact mills three cards from the target player's library")
    void sacrificeArtifactMillsThreeCards() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(station.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new GrindingStation());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Accepting the artifact trigger untaps Grinding Station")
    void artifactEnteringUntapsStation() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        station.tap();
        castArtifactFor(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Artifacts entering under an opponent's control also trigger Grinding Station")
    void opponentArtifactEnteringUntapsStation() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        station.tap();
        castArtifactFor(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A non-artifact entering does not trigger Grinding Station")
    void nonArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrindingStation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castArtifactFor(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new GlazeFiend()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
