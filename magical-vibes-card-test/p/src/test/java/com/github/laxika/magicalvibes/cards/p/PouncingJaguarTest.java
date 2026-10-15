package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PouncingJaguar.class})
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

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentsUpkeep() {
        castAndResolvePouncingJaguar();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Pouncing Jaguar");
    }

    @Test
    @DisplayName("Entering the battlefield does not create an echo setup trigger")
    void enteringBattlefieldDoesNotCreateEchoSetupTrigger() {
        harness.castFromHand(player1, new PouncingJaguar(), "{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pouncing Jaguar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's upkeep does not consume the pending echo obligation")
    void echoStillTriggersAfterOpponentsUpkeep() {
        castAndResolvePouncingJaguar();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Pouncing Jaguar");
        harness.assertInGraveyard(player1, "Pouncing Jaguar");
    }

    private void castAndResolvePouncingJaguar() {
        harness.castFromHand(player1, new PouncingJaguar(), "{G}");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Pouncing Jaguar");
    }
}
