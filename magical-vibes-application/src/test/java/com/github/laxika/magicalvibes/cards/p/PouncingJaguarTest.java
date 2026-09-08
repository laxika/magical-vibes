package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PouncingJaguarTest extends BaseCardTest {

    @Test
    @DisplayName("Declining echo sacrifices Pouncing Jaguar at its next upkeep")
    void decliningEchoSacrificesPouncingJaguar() {
        castAndResolvePouncingJaguar();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Pouncing Jaguar");
        harness.assertInGraveyard(player1, "Pouncing Jaguar");
    }

    @Test
    @DisplayName("Paying echo keeps Pouncing Jaguar and echo does not trigger again")
    void payingEchoKeepsPouncingJaguarAndIsOneShot() {
        castAndResolvePouncingJaguar();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Pouncing Jaguar");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pouncing Jaguar");
    }

    private void castAndResolvePouncingJaguar() {
        harness.setHand(player1, List.of(new PouncingJaguar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pouncing Jaguar");
    }
}
