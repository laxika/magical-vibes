package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pyromatics.class, GruulNodorog.class, GruulTurf.class})
class PyromaticsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of());

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a creature")
    void dealsDamageToCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());

        castPyromatics(target.getId(), List.of());

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesCopiesForEachPayment() {
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of("{1}{R}", "{1}{R}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Replicate copy may choose a new player target")
    void replicateCopyMayTargetAnotherPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of("{1}{R}"));

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Replicate payments require the full {1}{R} cost")
    void cannotPayReplicateWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstantWithRepeatedCosts(
                player1, 0, player2.getId(), List.of("{1}{R}")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GruulTurf()).getId();
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Replicate copies can independently keep or change their targets")
    void replicateCopiesMayIndependentlyTargetCreatureAndPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());
        harness.setLife(player2, 20);
        castPyromatics(player2.getId(), List.of("{1}{R}", "{1}{R}"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Pyromatics");
    }

    @Test
    @DisplayName("Copies deal lethal damage before remaining spells lose their target")
    void replicateCopiesKillCreatureBeforeOriginalResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GruulNodorog());
        castPyromatics(target.getId(), List.of("{1}{R}", "{1}{R}", "{1}{R}", "{1}{R}", "{1}{R}"));

        harness.passBothPriorities();
        for (int i = 0; i < 5; i++) {
            harness.handleMayAbilityChosen(player1, false);
        }
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gruul Nodorog");
        harness.assertInGraveyard(player2, "Gruul Nodorog");
        harness.assertInGraveyard(player1, "Pyromatics");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    private void castPyromatics(UUID targetId, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 2 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, targetId, replicatePayments);
    }
}
