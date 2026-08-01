package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FaerieImpostorTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when it is the only creature its controller has")
    void autoSacrificesWithNoOtherCreature() {
        castImpostor();

        // No other creature to return — the payment is impossible, so no prompt at all
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Faerie Impostor");
        harness.assertInGraveyard(player1, "Faerie Impostor");
    }

    @Test
    @DisplayName("Opponent's creatures don't satisfy the requirement")
    void opponentCreaturesDontCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With another creature, returning it keeps Faerie Impostor")
    void returningAnotherCreatureKeepsImpostor() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the return sacrifices Faerie Impostor")
    void decliningSacrificesImpostor() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castImpostor();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Faerie Impostor");
        harness.assertInGraveyard(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Faerie Impostor itself is never offered as the creature to return")
    void impostorIsNotAValidReturnChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castImpostor();

        harness.handleMayAbilityChosen(player1, true);

        UUID impostorId = findPermanent(player1, "Faerie Impostor").getId();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(impostorId);
    }

    @Test
    @DisplayName("With multiple other creatures, only the chosen one is returned")
    void onlyChosenCreatureIsReturned() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SavannahLions());

        castImpostor();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Savannah Lions").getId());

        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Savannah Lions");
        harness.assertInHand(player1, "Savannah Lions");
    }

    private void castImpostor() {
        harness.setHand(player1, List.of(new FaerieImpostor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }
}
