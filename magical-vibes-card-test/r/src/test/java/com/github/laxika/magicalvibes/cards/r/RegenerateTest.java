package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Regenerate.class, RuneclawBear.class, HowlingMine.class, CrawWurm.class, DoomBlade.class})
class RegenerateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Regenerate puts it on the stack targeting a creature")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, bearId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Regenerate");
        assertThat(entry.getTargetId()).isEqualTo(bearId);
    }

    @Test
    @DisplayName("Resolving Regenerate grants regeneration shield to target creature")
    void resolvingGrantsRegenerationShield() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield from Regenerate saves creature from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        // The shielded bear blocks a ground creature with lethal power.
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setBlocking(true);
        bear.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new CrawWurm());
        attacker.setAttacking(true);
        resolveCombat(player2);

        // Bears should survive via regeneration
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        Permanent survivedBear = findPermanent(player1, "Runeclaw Bear");
        assertThat(survivedBear.isTapped()).isTrue();
        assertThat(survivedBear.getRegenerationShield()).isEqualTo(0);
        assertThat(survivedBear.getMarkedDamage()).isZero();
        assertThat(survivedBear.isBlocking()).isFalse();
        assertThat(survivedBear.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Regenerate can target opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player2, "Runeclaw Bear");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player2.getId()).getFirst();
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate fizzles if target creature is removed")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.castInstant(player1, 0, bearId);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot cast Regenerate without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));

        UUID bearId = harness.getPermanentId(player1, "Runeclaw Bear");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Regenerate")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Howling Mine");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void grantingShieldDoesNotImmediatelyTapOrHealCreature() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        bear.setMarkedDamage(1);
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(bear.isTapped()).isFalse();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(bear.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void shieldReplacesOnlyTheNextDestruction() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.setHand(player2, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    void unusedShieldExpiresAtEndOfTurn() {
        Permanent bear = addCreatureReady(player1, new RuneclawBear());
        harness.setHand(player1, List.of(new Regenerate()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        assertThat(bear.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getRegenerationShield()).isZero();
    }
}
