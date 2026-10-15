package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.a.AzoriusKeyrune;
import com.github.laxika.magicalvibes.cards.c.CatacombSlug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieImpostor.class, AxebaneStag.class, CatacombSlug.class, AzoriusKeyrune.class})
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
        harness.addToBattlefield(player2, new AxebaneStag());

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player2, "Axebane Stag");
    }

    @Test
    @DisplayName("With another creature, returning it keeps Faerie Impostor")
    void returningAnotherCreatureKeepsImpostor() {
        harness.addToBattlefield(player1, new AxebaneStag());

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID stagId = harness.getPermanentId(player1, "Axebane Stag");
        harness.handlePermanentChosen(player1, stagId);

        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertNotOnBattlefield(player1, "Axebane Stag");
        harness.assertInHand(player1, "Axebane Stag");
    }

    @Test
    @DisplayName("Declining the return sacrifices Faerie Impostor")
    void decliningSacrificesImpostor() {
        harness.addToBattlefield(player1, new AxebaneStag());

        castImpostor();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Faerie Impostor");
        harness.assertInGraveyard(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Axebane Stag");
    }

    @Test
    @DisplayName("Faerie Impostor itself is never offered as the creature to return")
    void impostorIsNotAValidReturnChoice() {
        harness.addToBattlefield(player1, new AxebaneStag());

        castImpostor();

        harness.handleMayAbilityChosen(player1, true);

        UUID impostorId = harness.getPermanentId(player1, "Faerie Impostor");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(impostorId);
    }

    @Test
    @DisplayName("With multiple other creatures, only the chosen one is returned")
    void onlyChosenCreatureIsReturned() {
        harness.addToBattlefield(player1, new AxebaneStag());
        harness.addToBattlefield(player1, new CatacombSlug());

        castImpostor();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Catacomb Slug"));

        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Axebane Stag");
        harness.assertNotOnBattlefield(player1, "Catacomb Slug");
        harness.assertInHand(player1, "Catacomb Slug");
    }

    @Test
    @DisplayName("An animated Keyrune can be returned as the only other creature")
    void animatedArtifactCanBeReturned() {
        UUID keyruneId = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune()).getId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, keyruneId);

        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertInHand(player1, "Azorius Keyrune");
        harness.assertNotOnBattlefield(player1, "Azorius Keyrune");
    }

    @Test
    @DisplayName("An animated Keyrune is offered alongside printed creatures")
    void animatedArtifactIsIncludedInReturnChoices() {
        UUID keyruneId = harness.addToBattlefieldAndReturn(player1, new AzoriusKeyrune()).getId();
        harness.addToBattlefield(player1, new AxebaneStag());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        castImpostor();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(keyruneId);
        harness.handlePermanentChosen(player1, keyruneId);
        harness.assertOnBattlefield(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Axebane Stag");
        harness.assertInHand(player1, "Azorius Keyrune");
    }

    @Test
    @DisplayName("An unanimated artifact cannot satisfy the creature return requirement")
    void unanimatedArtifactDoesNotCount() {
        harness.addToBattlefield(player1, new AzoriusKeyrune());

        castImpostor();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Faerie Impostor");
        harness.assertOnBattlefield(player1, "Azorius Keyrune");
    }

    private void castImpostor() {
        harness.setHand(player1, List.of(new FaerieImpostor()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }
}
