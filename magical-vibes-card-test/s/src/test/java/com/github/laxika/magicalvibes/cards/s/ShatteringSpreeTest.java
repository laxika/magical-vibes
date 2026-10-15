package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShatteringSpree.class, GruulSignet.class, GhostWarden.class})
class ShatteringSpreeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        castShatteringSpree(List.of(), artifact.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player2, "Gruul Signet");
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        castShatteringSpree(List.of("{R}", "{R}"), artifact.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gruul Signet");
    }

    @Test
    @DisplayName("Replicate copy may choose a new artifact target")
    void replicateCopyMayTargetAnotherArtifact() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        castShatteringSpree(List.of("{R}"), originalTarget.getId());

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(originalTarget.getId())
                        || permanent.getId().equals(copyTarget.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GhostWarden());

        assertThatThrownBy(() -> castShatteringSpree(List.of(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by the caster")
    void destroysOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        castShatteringSpree(List.of(), artifact.getId());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gruul Signet");
        harness.assertInGraveyard(player1, "Gruul Signet");
    }

    @Test
    @DisplayName("Replicate payment requires mana in addition to the spell cost")
    void cannotReplicateWithoutEnoughMana() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        harness.setHand(player1, List.of(new ShatteringSpree()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstantWithRepeatedCosts(
                player1, 0, artifact.getId(), List.of("{R}")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Gruul Signet");
    }

    @Test
    @DisplayName("A replicate copy can retarget when the original target has left the battlefield")
    void copyCanRetargetAfterOriginalTargetLeaves() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        castShatteringSpree(List.of("{R}"), originalTarget.getId());
        gd.playerBattlefields.get(player2.getId()).remove(originalTarget);
        gd.playerGraveyards.get(player2.getId()).add(originalTarget.getCard());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof ShatteringSpree).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card instanceof GruulSignet).hasSize(2);
    }
    private void castShatteringSpree(List<String> replicatePayments, UUID targetId) {
        harness.setHand(player1, List.of(new ShatteringSpree()));
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
