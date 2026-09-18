package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlindCreeper;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrindingStation.class, ConjurersBauble.class, BlindCreeper.class})
class GrindingStationTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact mills three cards from the target player's library")
    void sacrificeArtifactMillsThreeCards() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        harness.addToBattlefield(player1, new ConjurersBauble());
        harness.setLibrary(player2, List.of(
                new BlindCreeper(), new BlindCreeper(), new BlindCreeper(), new BlindCreeper()));

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardBefore = gd.playerGraveyards.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Conjurer's Bauble"));

        assertThat(station.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Conjurer's Bauble");
        harness.assertInGraveyard(player1, "Conjurer's Bauble");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardBefore + 3);
    }

    @Test
    @DisplayName("Grinding Station can be sacrificed to pay its own activation cost")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new GrindingStation());
        harness.setLibrary(player2, List.of(
                new BlindCreeper(), new BlindCreeper(), new BlindCreeper(), new BlindCreeper()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grinding Station");
        harness.assertInGraveyard(player1, "Grinding Station");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
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
    @DisplayName("Declining the artifact trigger leaves Grinding Station tapped")
    void decliningArtifactTriggerLeavesStationTapped() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new GrindingStation());
        station.tap();
        castArtifactFor(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(station.isTapped()).isTrue();
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
        harness.castFromHand(player1, new BlindCreeper(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castArtifactFor(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new ConjurersBauble(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
