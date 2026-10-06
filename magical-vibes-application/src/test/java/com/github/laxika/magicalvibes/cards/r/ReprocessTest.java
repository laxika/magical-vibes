package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheVault;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IvoryCup;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Opposition;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiscipleOfTheVault.class, Forest.class, GrizzlyBears.class, Island.class, IvoryCup.class,
        Millstone.class, Opposition.class, Reprocess.class})
class ReprocessTest extends BaseCardTest {

    @Test
    @DisplayName("Only artifacts, creatures, and lands the controller controls are sacrificeable")
    void promptsSacrificeChoiceForEligibleTypes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IvoryCup());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Opposition()); // enchantment — not eligible
        setupLibrary();
        castReprocess();

        harness.passBothPriorities(); // resolve Reprocess

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                artifact.getId(), creature.getId(), land.getId());
    }

    @Test
    @DisplayName("Only artifacts, creatures, and lands the controller controls are sacrificeable")
    void promptsSacrificeChoiceForEligibleTypesUpstreamReview() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Opposition()); // enchantment — not eligible
        setupLibrary();
        castReprocess();

        harness.passBothPriorities(); // resolve Reprocess

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                artifact.getId(), creature.getId(), land.getId());
        assertThat(choice.validIds()).doesNotContain(opponentArtifact.getId(), opponentCreature.getId(), opponentLand.getId());
    }

    @Test
    @DisplayName("Opponent permanents are not eligible for the controller's sacrifice choice")
    void excludesOpponentsEligiblePermanents() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IvoryCup());
        setupLibrary();
        castReprocess();

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ownLand.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId(), opponentArtifact.getId());
    }

    @Test
    @DisplayName("Draws a card for each permanent sacrificed")
    void drawsPerPermanentSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new IvoryCup());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), land.getId()));

        // The two chosen permanents are gone; the artifact remains
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        // Two permanents sacrificed → two cards drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws a card for each permanent sacrificed")
    void drawsPerPermanentSacrificedUpstreamReview() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Millstone());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), land.getId()));

        // The two chosen permanents are gone; the artifact remains
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        // Two permanents sacrificed → two cards drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing nothing draws nothing and keeps all permanents")
    void sacrificeNoneDrawsNothing() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible permanents, the spell resolves with no prompt")
    void noEligiblePermanentsNoPrompt() {
        harness.addToBattlefieldAndReturn(player1, new Opposition()); // enchantment only
        setupLibrary();
        castReprocess();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Artifacts can be sacrificed along with creatures and lands, drawing for all three")
    void sacrificesAllEligibleTypesAndDrawsForEach() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Opposition());
        setupLibrary();
        castReprocess();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1,
                List.of(artifact.getId(), creature.getId(), land.getId()));

        harness.assertInGraveyard(player1, "Millstone");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Opposition");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature sacrificed with an artifact still sees that artifact go to the graveyard")
    @CardUsed({DiscipleOfTheVault.class})
    void sacrificedDiscipleSeesSimultaneouslySacrificedArtifact() {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DiscipleOfTheVault());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Millstone());
        setupLibrary();
        castReprocess();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(disciple.getId(), artifact.getId()));

        harness.assertInGraveyard(player1, "Disciple of the Vault");
        harness.assertInGraveyard(player1, "Millstone");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
    }

    private void castReprocess() {
        harness.castFromHand(player1, new Reprocess(), "{2}{B}{B}");
    }
}
