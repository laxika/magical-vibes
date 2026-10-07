package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFifteenthDoctor.class, GrizzlyBears.class, HowlingMine.class,
        CommandersSphere.class, SolRing.class, SolemnSimulacrum.class})
class TheFifteenthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering mills three cards and may return a milled artifact with mana value two or three")
    void enteringMillsAndReturnsMatchingArtifact() {
        Card artifact = new HowlingMine();
        Card nonArtifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact, nonArtifact, new GrizzlyBears()));
        harness.castFromHand(player1, new TheFifteenthDoctor(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Howling Mine");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(nonArtifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The first nonartifact spell each turn can use improvise")
    void firstNonartifactSpellGetsImproviseOnly() {
        harness.addToBattlefield(player1, new TheFifteenthDoctor());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(firstArtifact.getId()));
        harness.passBothPriorities();

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingCanReturnManaValueThreeArtifact() {
        addCreatureReady(player1, new TheFifteenthDoctor());
        Card sphere = new CommandersSphere();
        Card ring = new SolRing();
        Card nonartifact = new TheFifteenthDoctor();
        harness.setLibrary(player1, List.of(sphere, ring, nonartifact));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(sphere);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ring, nonartifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineFirstArtifactAndChooseAnotherButReturnsOnlyOne() {
        Card first = new HowlingMine();
        Card second = new CommandersSphere();
        Card third = new HowlingMine();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.enterBattlefieldAndReturn(player1, new TheFifteenthDoctor());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningReturnStillMillsAllThreeCards() {
        Card artifact = new HowlingMine();
        Card ring = new SolRing();
        Card doctor = new TheFifteenthDoctor();
        harness.setLibrary(player1, List.of(artifact, ring, doctor));
        harness.enterBattlefieldAndReturn(player1, new TheFifteenthDoctor());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact, ring, doctor);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Howling Mine");
    }

    @Test
    void shortLibraryWithNoEligibleCardsDoesNotOfferOldGraveyardArtifact() {
        Card oldArtifact = new HowlingMine();
        Card ring = new SolRing();
        harness.setGraveyard(player1, List.of(oldArtifact));
        harness.setLibrary(player1, List.of(ring));
        harness.enterBattlefieldAndReturn(player1, new TheFifteenthDoctor());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldArtifact, ring);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Howling Mine");
    }

    @Test
    void castingArtifactDoesNotConsumeFirstNonartifactGrant() {
        harness.addToBattlefield(player1, new TheFifteenthDoctor());
        harness.setHand(player1, List.of(new HowlingMine(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent artifact = findPermanent(player1, "Howling Mine");
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    void nonartifactCastBeforeDoctorEnteredAlreadyConsumesGrant() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TheFifteenthDoctor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void artifactsOutsideManaValueRangeCannotBeReturned() {
        Card ring = new SolRing();
        Card simulacrum = new SolemnSimulacrum();
        Card nonartifact = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ring, simulacrum, nonartifact));
        harness.enterBattlefieldAndReturn(player1, new TheFifteenthDoctor());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ring, simulacrum, nonartifact);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Sol Ring");
        harness.assertNotInHand(player1, "Solemn Simulacrum");
    }

    @Test
    void opponentsDoctorDoesNotGrantImprovise() {
        harness.addToBattlefield(player2, new TheFifteenthDoctor());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCard(
                gd, player1, 0, 0, null, null, List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
    }
}
