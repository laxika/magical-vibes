package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlissaTheTraitor.class, GrizzlyBears.class, LeoninScimitar.class, CruelEdict.class, Shock.class})
class GlissaTheTraitorTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers may ability when opponent's creature dies")
    void triggersWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Does not trigger when controller's own creature dies")
    void doesNotTriggerWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Glissa, the Traitor"));
    }

    @Test
    @DisplayName("Accepting may ability and choosing artifact returns it from graveyard to hand")
    void acceptingReturnsArtifactToHand() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Declining may ability does not return artifact")
    void decliningDoesNotReturnArtifact() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Trigger is removed when there is no legal artifact target")
    void noEffectWithNoArtifactsInGraveyard() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    @Test
    @DisplayName("Triggers when opponent's creature is killed by damage spell")
    void triggersWhenOpponentCreatureKilledByDamage() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearsId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Artifacts in an opponent's graveyard are not legal targets")
    void cannotTargetOpponentsArtifact() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Nonartifact cards in the controller's graveyard are not legal targets")
    void cannotTargetNonartifactCard() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal target cannot be replaced with another artifact during resolution")
    void cannotChooseAnotherArtifactWhenTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new GlissaTheTraitor());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar(), new LeoninScimitar()));
        var remainingArtifact = gd.playerGraveyards.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.setGraveyard(player1, List.of(remainingArtifact));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
