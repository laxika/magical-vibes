package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarnessedSnubhorn.class, TormodsCrypt.class, Pacifism.class, GrizzlyBears.class})
class HarnessedSnubhornTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage targets an artifact or enchantment card and returns it to the battlefield")
    void returnsTargetArtifactOrEnchantment() {
        Card artifact = new TormodsCrypt();
        Card enchantment = new Pacifism();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, enchantment, creature));

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId(), enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(enchantment, creature);
    }

    @Test
    @DisplayName("The combat-damage trigger is not created without a matching graveyard card")
    void noMatchingGraveyardCardProducesNoTrigger() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        dealCombatDamage();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("A returned Aura enters attached to a creature chosen by its controller")
    void returnsAuraAttachedToChosenCreature() {
        Card enchantment = new Pacifism();
        Permanent host = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(enchantment));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, host.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantment.getId())
                        && host.getId().equals(permanent.getAttachedTo()));
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Only the controller's graveyard supplies targets")
    void excludesOpponentsGraveyard() {
        Card ownArtifact = new TormodsCrypt();
        Card opposingArtifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(ownArtifact));
        harness.setGraveyard(player2, List.of(opposingArtifact));

        dealCombatDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownArtifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownArtifact.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Tormod's Crypt");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingArtifact);
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution is not returned")
    void doesNotReturnRemovedTarget() {
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(artifact));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(artifact));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Tormod's Crypt");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The controller must choose a target when a legal graveyard card exists")
    void cannotDeclineMandatoryReturn() {
        Card artifact = new TormodsCrypt();
        harness.setGraveyard(player1, List.of(artifact));

        dealCombatDamage();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Tormod's Crypt");
    }

    private void dealCombatDamage() {
        Permanent snubhorn = addCreatureReady(player1, new HarnessedSnubhorn());
        snubhorn.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
