package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(UrzasBlueprints.class)
class UrzasBlueprintsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Urza's Blueprints draws a card")
    void tappingDrawsCard() {
        Permanent blueprints = harness.addToBattlefieldAndReturn(player1, new UrzasBlueprints());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new UrzasBlueprints()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(blueprints.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .anyMatch(card -> card instanceof UrzasBlueprints);
    }

    @Test
    @DisplayName("Urza's Blueprints cannot be tapped twice without untapping")
    void cannotActivateWhenTapped() {
        Permanent blueprints = harness.addToBattlefieldAndReturn(player1, new UrzasBlueprints());
        blueprints.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Urza's Blueprints can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent blueprints = harness.addToBattlefieldAndReturn(player1, new UrzasBlueprints());
        blueprints.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new UrzasBlueprints()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(blueprints.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining echo sacrifices Urza's Blueprints at its next upkeep")
    void decliningEchoSacrificesBlueprints() {
        castAndResolveBlueprints();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Urza's Blueprints");
        harness.assertInGraveyard(player1, "Urza's Blueprints");
    }

    @Test
    @DisplayName("Paying echo keeps Urza's Blueprints and echo does not trigger again")
    void payingEchoKeepsBlueprintsAndIsOneShot() {
        castAndResolveBlueprints();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Urza's Blueprints");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Urza's Blueprints");
    }

    @Test
    @DisplayName("Echo waits for Urza's Blueprints' controller's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveBlueprints();

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Urza's Blueprints");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castAndResolveBlueprints() {
        harness.castFromHand(player1, new UrzasBlueprints(), "{6}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Urza's Blueprints");
    }
}
