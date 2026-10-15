package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ResoundingRoar.class, CylianElf.class, MagmaSpray.class})
class ResoundingRoarTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +3/+3 until end of turn")
    void boostsTargetCreature() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent elf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elf.getEffectivePower()).isEqualTo(5);
        assertThat(elf.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elf.getPowerModifier()).isEqualTo(0);
        assertThat(elf.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cycling gives target creature +6/+6 and draws a card")
    void cyclingBoostsTargetAndDraws() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.activateHandAbility(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent elf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(elf.getEffectivePower()).isEqualTo(8);
        assertThat(elf.getEffectiveToughness()).isEqualTo(8);
        // Resolve the separate cycling draw after the boost trigger.
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Resounding Roar");
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling draws even when no creature exists")
    void cyclesWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        addCyclingMana(player1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Resounding Roar");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("Cycling trigger resolves before the separate draw with priority between them")
    void cyclingBoostResolvesBeforeDraw() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        addCyclingMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Cylian Elf");
        harness.activateHandAbility(player1, 0, targetId);
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());

        Permanent elf = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elf.getEffectivePower()).isEqualTo(8);
        assertThat(elf.getEffectiveToughness()).isEqualTo(8);
        harness.assertNotInHand(player1, "Cylian Elf");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(elf.getPowerModifier()).isZero();
        assertThat(elf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Losing the cycling trigger's target does not prevent the cycling draw")
    void cyclingDrawsAfterTargetLeaves() {
        harness.addToBattlefield(player1, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.setHand(player2, List.of(new MagmaSpray()));
        addCyclingMana(player1);
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Cylian Elf");
        harness.activateHandAbility(player1, 0, targetId);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertNotOnBattlefield(player1, "Cylian Elf");

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");
        harness.assertInGraveyard(player1, "Resounding Roar");
    }

    @Test
    @DisplayName("The spell can boost an opponent's creature")
    void spellBoostsOpponentsCreature() {
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player1, List.of(new ResoundingRoar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Cylian Elf"));

        Permanent elf = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(elf.getEffectivePower()).isEqualTo(5);
        assertThat(elf.getEffectiveToughness()).isEqualTo(5);
    }

    private void addCyclingMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }
}
