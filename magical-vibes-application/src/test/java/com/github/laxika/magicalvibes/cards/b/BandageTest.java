package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bandage.class, BallistaSquad.class, GrizzlyBears.class})
class BandageTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bandage puts it on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Bandage");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast Bandage without enough mana")
    void cannotCastWithoutMana() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving Bandage adds prevention shield to target creature")
    void resolvingAddsPrevention() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getDamagePreventionShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevention shield prevents 1 combat damage to creature")
    void preventionShieldPrevents1CombatDamage() {
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Bandage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, defender.getId());

        attacker.setAttacking(true);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(defender.getMarkedDamage()).isEqualTo(1);
        assertThat(defender.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Prevention shield is consumed after preventing damage")
    void preventionShieldIsConsumed() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setDamagePreventionShield(1);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();

        Permanent surviving = findPermanent(player2, "Grizzly Bears");
        assertThat(surviving.getMarkedDamage()).isEqualTo(1);
        assertThat(surviving.getDamagePreventionShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving Bandage targeting a player adds prevention shield")
    void resolvingAddsPlayerPrevention() {
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Player prevention shield reduces combat damage by 1")
    void playerPreventionReducesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Bandage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving Bandage draws a card for the caster")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears deckCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(deckCard));

        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Hand should be empty after casting (Bandage was the only card)
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        // After resolving, player should have drawn a card
        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Bandage goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player1, "Bandage");
    }

    @Test
    @DisplayName("Prevention shield reduces activated ability damage to creature")
    void preventionReducesAbilityDamage() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setAttacking(true);

        addCreatureReady(player1, new BallistaSquad());

        harness.setHand(player1, List.of(new Bandage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Bandage fizzles entirely if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Bandage()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Entire spell fizzles — no draw happens
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Fizzled spell still goes to graveyard
        harness.assertInGraveyard(player1, "Bandage");
    }

    @Test
    @DisplayName("Prevention shields are cleared at end of turn")
    void preventionShieldsClearedAtEndOfTurn() {
        Permanent perm = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bandage(), new Bandage()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, perm.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(afterCleanup.getDamagePreventionShield()).isEqualTo(0);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }
}

