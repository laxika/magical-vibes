package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterSkald.class, GrizzlyBears.class, Pacifism.class, Spellbook.class, Ornithopter.class})
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

    @Test
    @DisplayName("Returns a targeted enchantment after exiling a creature")
    void returnsEnchantment() {
        GrizzlyBears creature = new GrizzlyBears();
        Pacifism enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        harness.assertInHand(player1, "Pacifism");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot return the target without a creature card to exile")
    void noCreatureToExileDoesNotReturnTarget() {
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotInHand(player1, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile a creature when there is no legal return target")
    void noLegalReturnTargetDoesNotExileCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(new Spellbook(), new Pacifism()));

        castMasterSkald();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Pacifism");
    }

    @Test
    @DisplayName("Exiling the targeted artifact creature does not return it")
    void canExileTargetedArtifactCreature() {
        Ornithopter creature = new Ornithopter();
        harness.setGraveyard(player1, List.of(creature));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        harness.assertNotInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An illegal return target prevents the entire ability from resolving")
    void targetLeavingGraveyardPreventsCreatureExile() {
        GrizzlyBears creature = new GrizzlyBears();
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(creature, artifact));

        castMasterSkald();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of(creature));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(artifact);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Spellbook");
    }

    private void castMasterSkald() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MasterSkald(), "{4}{W}");
        harness.passBothPriorities();
    }
}
