package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterSkaldTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature, then returns the targeted artifact or enchantment")
    void exilesCreatureThenReturnsTargetedPermanentCard() {
        GrizzlyBears creature = new GrizzlyBears();
        Spellbook artifact = new Spellbook();
        Pacifism enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, artifact, enchantment));

        castMasterSkald();

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the may ability does not exile or return a card")
    void mayDeclineDoesNothing() {
        GrizzlyBears creature = new GrizzlyBears();
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(creature, artifact));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Choosing among multiple creature cards is mandatory")
    void choosesCreatureToExile() {
        GrizzlyBears firstCreature = new GrizzlyBears();
        GrizzlyBears secondCreature = new GrizzlyBears();
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature, artifact));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice exileChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(exileChoice).isNotNull();
        assertThat(exileChoice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(firstCreature));

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(secondCreature);
    }

    private void castMasterSkald() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MasterSkald()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
