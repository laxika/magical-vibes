package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BenedictionOfMoons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyromatics;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DjinnIlluminatus.class, Shock.class, GrizzlyBears.class, BenedictionOfMoons.class, Pyromatics.class})
class DjinnIlluminatusTest extends BaseCardTest {

    @Test
    @DisplayName("Grants replicate to an instant at its mana cost")
    void grantsReplicateToInstantAtItsManaCost() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithRepeatedCosts(player1, 0, player2.getId(), List.of("{R}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Grants replicate to a sorcery at its mana cost")
    void grantsReplicateToSorceryAtItsManaCost() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{W}"), List.of());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not grant replicate to a creature spell")
    void doesNotGrantReplicateToCreatureSpell() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Replicate is optional")
    void replicateIsOptional() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Each replicate payment creates one copy")
    void eachReplicatePaymentCreatesOneCopy() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of("{W}", "{W}"), List.of());
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        resolveAllTriggers();
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Does not grant replicate to an opponent's spell")
    void doesNotGrantReplicateToOpponentsSpell() {
        harness.addToBattlefield(player2, new DjinnIlluminatus());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new BenedictionOfMoons()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorceryWithRepeatedCosts(player1, 0, List.of(), List.of());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Printed replicate still creates copies with Djinn Illuminatus")
    void printedReplicateStillCreatesCopies() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pyromatics()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstantWithRepeatedCosts(player1, 0, player2.getId(), List.of("{1}{R}"));
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A granted replicate copy may choose a new target")
    void grantedReplicateCopyMayChooseNewTarget() {
        harness.addToBattlefield(player1, new DjinnIlluminatus());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithRepeatedCosts(player1, 0, player2.getId(), List.of("{R}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }
}
