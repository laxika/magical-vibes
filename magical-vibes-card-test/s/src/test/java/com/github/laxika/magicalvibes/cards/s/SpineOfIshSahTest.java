package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpineOfIshSah.class, GrizzlyBears.class, LeoninScimitar.class, Shatter.class, DivineOffering.class})
class SpineOfIshSahTest extends BaseCardTest {


    @Test
    @DisplayName("Spine of Ish Sah is cast without a target even on an empty battlefield")
    void castingPutsUntargetedArtifactOnStack() {
        harness.castFromHand(player1, new SpineOfIshSah(), "{7}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving Spine of Ish Sah enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> enters battlefield, ETB triggers
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spine of Ish Sah");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target permanent")
    void etbDestroysTargetPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB can target and destroy an artifact")
    void etbDestroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.castArtifact(player1, 0, targetId);

        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }


    @Test
    @DisplayName("Indestructible permanent survives ETB")
    void indestructiblePermanentSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> ETB on stack
        harness.passBothPriorities();

        // Grant indestructible before ETB resolves
        Permanent target = findPermanent(player2, "Grizzly Bears");
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }


    @Test
    @DisplayName("ETB fizzles if target is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpineOfIshSah()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        // Resolve artifact spell -> ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB -> fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }


    @Test
    @DisplayName("Destroying Spine of Ish Sah returns it to owner's hand")
    void deathTriggerReturnsToHand() {
        harness.addToBattlefield(player1, new SpineOfIshSah());

        // Use Shatter to destroy the Spine
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID targetId = harness.getPermanentId(player1, "Spine of Ish Sah");
        harness.castAndResolveInstant(player2, 0, targetId);

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve death trigger
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Spine should be in hand, NOT in graveyard
        harness.assertInHand(player1, "Spine of Ish Sah");
        harness.assertNotInGraveyard(player1, "Spine of Ish Sah");
    }

    @Test
    @DisplayName("Spine can target itself after entering and returns to hand after destroying itself")
    void canDestroyItselfWithItsEtb() {
        harness.castFromHand(player1, new SpineOfIshSah(), "{7}");
        harness.passBothPriorities();
        UUID spineId = harness.getPermanentId(player1, "Spine of Ish Sah");
        harness.handlePermanentChosen(player1, spineId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spine of Ish Sah");
        harness.assertInGraveyard(player1, "Spine of Ish Sah");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Spine of Ish Sah");
        harness.assertNotInGraveyard(player1, "Spine of Ish Sah");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen Spine returns to its owner's hand rather than remaining in that owner's graveyard")
    void deathTriggerReturnsStolenSpineToOwner() {
        SpineOfIshSah spine = new SpineOfIshSah();
        spine.setOwnerId(player1.getId());
        UUID spineId = harness.addToBattlefieldAndReturn(player2, spine).getId();
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, spineId);

        harness.assertInGraveyard(player1, "Spine of Ish Sah");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        harness.assertInHand(player1, "Spine of Ish Sah");
        harness.assertNotInHand(player2, "Spine of Ish Sah");
        harness.assertNotInGraveyard(player1, "Spine of Ish Sah");
    }

    @Test
    @DisplayName("The return ability returns only the Spine that died")
    void deathTriggerDoesNotReturnOtherCopies() {
        SpineOfIshSah otherSpine = new SpineOfIshSah();
        harness.setGraveyard(player1, List.of(otherSpine));
        SpineOfIshSah dyingSpine = new SpineOfIshSah();
        UUID spineId = harness.addToBattlefieldAndReturn(player1, dyingSpine).getId();
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, spineId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(dyingSpine).doesNotContain(otherSpine);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherSpine);
    }

    @Test
    @DisplayName("The destroy ability still resolves after Spine is destroyed and returned to hand")
    void etbResolvesAfterSourceLeavesBattlefield() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SpineOfIshSah()).getId();
        harness.castFromHand(player1, new SpineOfIshSah(), "{7}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        UUID sourceId = harness.getPermanentId(player1, "Spine of Ish Sah");
        harness.setHand(player1, List.of(new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, sourceId);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Spine of Ish Sah");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spine of Ish Sah");
        harness.assertInGraveyard(player2, "Spine of Ish Sah");
        harness.passBothPriorities();
        harness.assertInHand(player2, "Spine of Ish Sah");
    }
}
