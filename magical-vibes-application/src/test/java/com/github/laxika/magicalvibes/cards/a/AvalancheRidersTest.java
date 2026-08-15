package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvalancheRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield destroys a target land")
    void etbDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveAvalancheRiders(harness.getPermanentId(player2, "Forest"));

        harness.assertOnBattlefield(player1, "Avalanche Riders");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AvalancheRiders()));
        addCastMana();

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Declining echo sacrifices Avalanche Riders")
    void decliningEchoSacrificesAvalancheRiders() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveAvalancheRiders(harness.getPermanentId(player2, "Forest"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Avalanche Riders");
        harness.assertInGraveyard(player1, "Avalanche Riders");
    }

    @Test
    @DisplayName("Paying echo keeps Avalanche Riders and echo does not trigger again")
    void payingEchoKeepsAvalancheRidersAndIsOneShot() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveAvalancheRiders(harness.getPermanentId(player2, "Forest"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Avalanche Riders");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Avalanche Riders");
    }

    private void castAndResolveAvalancheRiders(UUID targetId) {
        harness.setHand(player1, List.of(new AvalancheRiders()));
        addCastMana();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
