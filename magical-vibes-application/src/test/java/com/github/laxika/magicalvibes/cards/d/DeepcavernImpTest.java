package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepcavernImp.class, FomoriNomad.class, DeathRattle.class})
class DeepcavernImpTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card pays echo and keeps Deepcavern Imp for one turn")
    void discardingPaysEchoAndEchoIsOneShot() {
        castAndResolveImp(true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Deepcavern Imp");
        harness.assertInGraveyard(player1, "Fomori Nomad");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Deepcavern Imp");
    }

    @Test
    @DisplayName("Declining echo sacrifices Deepcavern Imp")
    void decliningEchoSacrificesImp() {
        castAndResolveImp(true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Deepcavern Imp");
        harness.assertInGraveyard(player1, "Deepcavern Imp");
        harness.assertInHand(player1, "Fomori Nomad");
    }

    @Test
    @DisplayName("Empty hand causes Deepcavern Imp to be sacrificed without a prompt")
    void emptyHandSacrificesImpWithoutPrompt() {
        castAndResolveImp(false);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Deepcavern Imp");
        harness.assertInGraveyard(player1, "Deepcavern Imp");
    }

    @Test
    @DisplayName("Echo waits for Deepcavern Imp's controller's next upkeep")
    void echoTriggersOnlyAtControllersNextUpkeep() {
        castAndResolveImp(true);

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Deepcavern Imp");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Deepcavern Imp");
    }

    @Test
    @DisplayName("Leaving the battlefield before upkeep removes the echo obligation")
    void leavingBattlefieldBeforeUpkeepRemovesEchoObligation() {
        castAndResolveImp(false);
        harness.setHand(player1, List.of(new DeathRattle()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, findPermanent(player1, "Deepcavern Imp").getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Deepcavern Imp");
    }

    private void castAndResolveImp(boolean includeDiscardCard) {
        harness.setHand(player1, includeDiscardCard
                ? List.of(new DeepcavernImp(), new FomoriNomad())
                : List.of(new DeepcavernImp()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deepcavern Imp");
    }
}
