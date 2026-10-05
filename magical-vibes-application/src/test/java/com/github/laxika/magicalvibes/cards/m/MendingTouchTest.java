package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GruulCluestone;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.a.ArmoredWolfRider;
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

@CardUsed({MendingTouch.class, KraulWarrior.class, ArmoredWolfRider.class, GruulCluestone.class})
class MendingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mending Touch grants a regeneration shield to the target creature")
    void resolvingGrantsRegenerationShield() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Kraul Warrior");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves the creature from lethal combat damage")
    void shieldSavesFromLethalCombatDamage() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player1, "Kraul Warrior");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setBlocking(true);
        bear.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ArmoredWolfRider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kraul Warrior");
        Permanent survivor = findPermanent(player1, "Kraul Warrior");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
        assertThat(survivor.getMarkedDamage()).isZero();
        assertThat(survivor.isBlocking()).isFalse();
        assertThat(survivor.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Mending Touch can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID bearId = harness.getPermanentId(player2, "Kraul Warrior");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mending Touch cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GruulCluestone());
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player1, "Gruul Cluestone");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Creating a shield does not tap the creature or remove existing damage")
    void shieldDoesNotImmediatelyRegenerate() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        warrior.setMarkedDamage(1);
        harness.setHand(player1, List.of(new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        assertThat(warrior.isTapped()).isFalse();
        assertThat(warrior.getMarkedDamage()).isEqualTo(1);
        assertThat(warrior.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Separate Mending Touch spells grant independent regeneration shields")
    void multipleSpellsGrantMultipleShields() {
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new MendingTouch(), new MendingTouch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, warrior.getId());
        harness.castAndResolveInstant(player1, 0, warrior.getId());
        assertThat(warrior.getRegenerationShield()).isEqualTo(2);

        warrior.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Kraul Warrior");
        assertThat(warrior.getRegenerationShield()).isEqualTo(1);
        assertThat(warrior.getMarkedDamage()).isZero();

        warrior.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player1, "Kraul Warrior");
        assertThat(warrior.getRegenerationShield()).isZero();
        assertThat(warrior.getMarkedDamage()).isZero();

        warrior.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
        harness.assertInGraveyard(player1, "Kraul Warrior");
    }
}
