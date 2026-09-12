package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FodderCannon;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonVandals.class, FodderCannon.class, HulkingOgre.class})
class KeldonVandalsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and destroys the targeted artifact")
    void etbDestroysArtifact() {
        harness.addToBattlefield(player2, new FodderCannon());
        castAndResolveVandals(harness.getPermanentId(player2, "Fodder Cannon"));

        harness.assertOnBattlefield(player1, "Keldon Vandals");
        harness.assertNotOnBattlefield(player2, "Fodder Cannon");
        harness.assertInGraveyard(player2, "Fodder Cannon");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player2, new HulkingOgre());
        prepareVandals();

        assertThatThrownBy(() -> harness.castCreature(
                        player1, 0, 0, harness.getPermanentId(player2, "Hulking Ogre")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("Declining echo sacrifices Keldon Vandals at its next upkeep")
    void decliningEchoSacrificesVandals() {
        castAndResolveVandals(harness.addToBattlefieldAndReturn(player2, new FodderCannon()).getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Keldon Vandals");
        harness.assertInGraveyard(player1, "Keldon Vandals");
    }

    @Test
    @DisplayName("Paying echo keeps Keldon Vandals and echo does not trigger again")
    void payingEchoKeepsVandalsAndIsOneShot() {
        castAndResolveVandals(harness.addToBattlefieldAndReturn(player2, new FodderCannon()).getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Keldon Vandals");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Keldon Vandals");
    }

    @Test
    @DisplayName("Echo waits for Keldon Vandals' controller's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolveVandals(harness.addToBattlefieldAndReturn(player2, new FodderCannon()).getId());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Keldon Vandals");
    }

    private void prepareVandals() {
        harness.setHand(player1, List.of(new KeldonVandals()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void castAndResolveVandals(java.util.UUID targetId) {
        prepareVandals();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
