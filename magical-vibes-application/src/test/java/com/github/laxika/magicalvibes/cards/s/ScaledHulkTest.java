package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScaledHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell gives Scaled Hulk +2/+2")
    void spiritSpellBoostsScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(2);
        assertThat(hulk.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting an Arcane spell gives Scaled Hulk +2/+2")
    void arcaneSpellBoostsScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(2);
        assertThat(hulk.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Scaled Hulk")
    void unrelatedSpellDoesNotBoostScaledHulk() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boosts stack and wear off at end of turn")
    void boostsStackAndWearOffAtEndOfTurn() {
        Permanent hulk = addCreatureReady(player1, new ScaledHulk());
        harness.setHand(player1, List.of(new DesperateRitual(), new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isEqualTo(4);
        assertThat(hulk.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hulk.getPowerModifier()).isZero();
        assertThat(hulk.getToughnessModifier()).isZero();
    }
}
