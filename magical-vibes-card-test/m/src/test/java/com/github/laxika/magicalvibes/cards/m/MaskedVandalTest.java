package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaskedVandal.class, GrizzlyBears.class, GloriousAnthem.class, LeoninScimitar.class})
class MaskedVandalTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and the targeted opposing artifact")
    void exilesCreatureAndArtifact() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setGraveyard(player1, List.of(creature));

        castMaskedVandal();
        chooseTarget(artifact);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("Exiles the targeted opposing enchantment")
    void exilesEnchantment() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setGraveyard(player1, List.of(creature));

        castMaskedVandal();
        chooseTarget(enchantment);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("Declining the creature exile leaves both cards unchanged")
    void mayDeclineDoesNothing() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setGraveyard(player1, List.of(creature));

        castMaskedVandal();
        chooseTarget(artifact);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Only opposing artifacts and enchantments are legal targets")
    void restrictsTargets() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castMaskedVandal();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opposingArtifact.getId())
                .doesNotContain(ownArtifact.getId(), opposingCreature.getId());
    }

    @Test
    void cannotExileArtifactWithoutCreatureInOwnGraveyard() {
        LeoninScimitar noncreature = new LeoninScimitar();
        GrizzlyBears opponentsCreature = new GrizzlyBears();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));

        castMaskedVandal();
        chooseTarget(artifact);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void choosesExactlyOneCreatureAtResolution() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        LeoninScimitar noncreature = new LeoninScimitar();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setGraveyard(player1, List.of(noncreature, first, second));

        castMaskedVandal();
        chooseTarget(artifact);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.handleGraveyardCardChosen(player1, 2);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature, first);
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact.getCard());
    }

    @Test
    void illegalTargetPreventsCreatureExile() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        harness.setGraveyard(player1, List.of(creature));

        castMaskedVandal();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.setGraveyard(player2, List.of(artifact.getCard()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noLegalTargetDoesNotExileCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castMaskedVandal();

        harness.assertOnBattlefield(player1, "Masked Vandal");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void castMaskedVandal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MaskedVandal(), "{1}{G}");
        harness.passBothPriorities();
    }
}
