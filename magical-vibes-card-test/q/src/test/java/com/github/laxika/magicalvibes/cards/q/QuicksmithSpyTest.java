package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KariZevsExpertise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksmithSpy.class, AngelsFeather.class, DoomBlade.class, GrizzlyBears.class,
        Island.class, KariZevsExpertise.class})
class QuicksmithSpyTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted artifact gains a tap ability that draws a card")
    void artifactDrawsCard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());
        harness.setLibrary(player1, List.of(new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The granted ability ends when Quicksmith Spy leaves the battlefield")
    void grantedAbilityEndsWhenSpyLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());

        destroySpy(harness.getPermanentId(player1, "Quicksmith Spy"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The ETB target must be an artifact you control")
    void rejectsIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuicksmithSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you control");
    }

    @Test
    @DisplayName("An opponent's artifact cannot be chosen for the ETB ability")
    void rejectsOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        harness.setHand(player1, List.of(new QuicksmithSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact you control");
    }

    @Test
    @DisplayName("Only the targeted artifact receives the draw ability")
    void otherArtifactDoesNotGainAbility() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new AngelsFeather());
        castSpy(artifact.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("A tapped artifact cannot pay the granted ability's tap cost")
    void tappedArtifactCannotDrawAgain() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The artifact never gains the ability if the Spy leaves before its ETB resolves")
    void spyLeavesBeforeTriggerResolves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        harness.setHand(player1, List.of(new QuicksmithSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, artifact.getId());
        harness.passBothPriorities();
        destroySpy(harness.getPermanentId(player1, "Quicksmith Spy"));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("A draw ability already on the stack resolves after the Spy leaves")
    void pendingDrawSurvivesSpyLeaving() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());
        harness.setLibrary(player1, List.of(new Island()));
        harness.activateAbility(player1, 0, null, null);
        destroySpy(harness.getPermanentId(player1, "Quicksmith Spy"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Quicksmith Spy");
    }

    @Test
    @DisplayName("Losing control of the Spy permanently ends the granted ability")
    void losingControlEndsAbility() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());
        stealSpy();

        harness.assertOnBattlefield(player2, "Quicksmith Spy");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Regaining control of the Spy does not restore the granted ability")
    void regainingControlDoesNotRestoreAbility() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AngelsFeather());
        castSpy(artifact.getId());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        stealSpy();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Quicksmith Spy");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private void stealSpy() {
        UUID spyId = harness.getPermanentId(player1, "Quicksmith Spy");
        gd.activePlayerId = player2.getId();
        harness.setHand(player2, List.of(new KariZevsExpertise()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, spyId);
    }

    private void castSpy(UUID targetId) {
        harness.setHand(player1, List.of(new QuicksmithSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void destroySpy(UUID targetId) {
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
