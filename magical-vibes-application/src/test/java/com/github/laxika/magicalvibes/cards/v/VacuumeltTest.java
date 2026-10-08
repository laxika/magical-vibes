package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.Ghostway;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vacuumelt.class, GhostWarden.class, Ghostway.class})
class VacuumeltTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the target creature to its owner's hand")
    void returnsTargetCreatureToHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castVacuumelt(target, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
    }

    @Test
    @DisplayName("Replicate creates a copy that may target another creature")
    void replicateCopyMayTargetAnotherCreature() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castVacuumelt(originalTarget, List.of("{2}{U}"));

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertInHand(player1, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
    }

    @Test
    @DisplayName("Replicate creates one copy for each replicate payment")
    void replicateCreatesOneCopyForEachPayment() {
        harness.setHand(player2, List.of());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        Permanent firstCopyTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        Permanent secondCopyTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castVacuumelt(originalTarget, List.of("{2}{U}", "{2}{U}"));

        harness.passBothPriorities();
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopyTarget.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondCopyTarget.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Ghost Warden"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Declining to retarget still creates a copy with the original target")
    void replicateCopyKeepsOriginalTarget() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castVacuumelt(target, List.of("{2}{U}"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Ghost Warden"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Vacuumelt");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Vacuumelt"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Replicate can retarget its copy after the original target leaves")
    void replicateSurvivesOriginalTargetLeaving() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        Permanent copyTarget = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        castVacuumelt(originalTarget, List.of("{2}{U}"));
        harness.setHand(player1, List.of(new Ghostway()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
        harness.assertInGraveyard(player1, "Vacuumelt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner's hand")
    void returnsStolenCreatureToOwner() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        castVacuumelt(target, List.of());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Warden");
        harness.assertInHand(player2, "Ghost Warden");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Ghost Warden"));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Vacuumelt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    private void castVacuumelt(Permanent target, List<String> replicatePayments) {
        harness.setHand(player1, List.of(new Vacuumelt()));
        harness.addMana(player1, ManaColor.BLUE, 1 + replicatePayments.size());
        harness.addMana(player1, ManaColor.COLORLESS, 2 + replicatePayments.size() * 2);
        harness.castInstantWithRepeatedCosts(player1, 0, target.getId(), replicatePayments);
    }
}
