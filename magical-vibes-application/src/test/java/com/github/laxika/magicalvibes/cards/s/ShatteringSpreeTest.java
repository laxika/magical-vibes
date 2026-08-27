package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({ShatteringSpree.class, Ornithopter.class, GrizzlyBears.class})
class ShatteringSpreeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castShatteringSpree(List.of(), artifact.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        castShatteringSpree(List.of("{R}", "{R}"), artifact.getId());

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castShatteringSpree(List.of(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castShatteringSpree(List<String> replicatePayments, UUID targetId) {
        harness.setHand(player1, List.of(new ShatteringSpree()));
        harness.addMana(player1, ManaColor.RED, 1 + replicatePayments.size());
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
