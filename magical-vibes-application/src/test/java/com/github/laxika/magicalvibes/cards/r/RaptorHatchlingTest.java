package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaptorHatchling.class, Shock.class, GrizzlyBears.class})
class RaptorHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("When dealt spell damage, creates a 3/3 green Dinosaur token with trample")
    void spellDamageCreatesToken() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player2, "Raptor Hatchling");
        harness.castInstant(player1, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage kills the 1/1

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger
        harness.passBothPriorities();

        // Hatchling dies from lethal damage but trigger still resolves
        harness.assertNotOnBattlefield(player2, "Raptor Hatchling");

        // Token should be on the battlefield
        Permanent token = findPermanent(player2, "Dinosaur");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DINOSAUR);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("When dealt combat damage, creates a 3/3 Dinosaur token")
    void combatDamageCreatesToken() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent hatchling = findPermanent(player2, "Raptor Hatchling");
        hatchling.setSummoningSick(false);
        hatchling.setBlocking(true);
        hatchling.addBlockingTarget(0);

        // Resolve combat damage and trigger
        resolveCombat(player1);
        harness.passBothPriorities(); // trigger on stack
        harness.passBothPriorities(); // resolve trigger

        // Token should exist
        Permanent token = findPermanent(player2, "Dinosaur");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple damage events create multiple tokens")
    void multipleDamageEventsCreateMultipleTokens() {
        harness.addToBattlefield(player1, new RaptorHatchling());
        // Give it enough toughness to survive two Shocks (we'll use a second card for the second hit)
        // Actually, the hatchling is 1/1 so it dies from first Shock.
        // Instead, test with two Raptor Hatchlings each getting damaged.
        harness.addToBattlefield(player1, new RaptorHatchling());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        // Shock first hatchling
        List<Permanent> hatchlings = findPermanents(player1, "Raptor Hatchling");
        harness.castInstant(player2, 0, hatchlings.get(0).getId());
        harness.passBothPriorities(); // Resolve first Shock
        harness.passBothPriorities(); // Resolve first trigger

        // Shock second hatchling
        harness.castInstant(player2, 0, hatchlings.get(1).getId());
        harness.passBothPriorities(); // Resolve second Shock
        harness.passBothPriorities(); // Resolve second trigger

        // Should have two Dinosaur tokens
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(2);
    }

    @Test
    @DisplayName("Token is created under the Hatchling controller's control")
    void tokenBelongsToController() {
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player2, "Raptor Hatchling");
        harness.castInstant(player1, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock
        harness.passBothPriorities(); // Resolve trigger

        // Token should be on player2's battlefield, not player1's
        assertThat(countPermanents(player2, "Dinosaur")).isEqualTo(1);
        assertThat(countPermanents(player1, "Dinosaur")).isZero();
    }

    @Test
    @DisplayName("Trigger fires even when Hatchling dies from the damage")
    void triggerFiresEvenWhenCreatureDies() {
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID hatchlingId = harness.getPermanentId(player1, "Raptor Hatchling");
        harness.castInstant(player2, 0, hatchlingId);
        harness.passBothPriorities(); // Resolve Shock — kills the 1/1

        // Trigger should be on stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // Resolve trigger

        // Hatchling should be dead
        harness.assertNotOnBattlefield(player1, "Raptor Hatchling");

        // But the Dinosaur token should exist
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);
    }

    @Test
    @DisplayName("Simultaneous damage from two blockers triggers enrage only once")
    void simultaneousCombatDamageTriggersOnce() {
        Permanent hatchling = addCreatureReady(player1, new RaptorHatchling());
        hatchling.setAttacking(true);
        for (int i = 0; i < 2; i++) {
            Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
        }

        resolveCombat(player1);
        if (gd.interaction.isAwaitingInput()) {
            List<Permanent> blockers = findPermanents(player2, "Grizzly Bears");
            harness.handleCombatDamageAssigned(player1, 0,
                    Map.of(blockers.get(0).getId(), 1, blockers.get(1).getId(), 0));
        }
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Raptor Hatchling");
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(1);
    }
}
