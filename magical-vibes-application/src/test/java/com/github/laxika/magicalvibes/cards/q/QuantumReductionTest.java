package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuantumReduction.class, GrizzlyBears.class, LlanowarElves.class, Island.class})
class QuantumReductionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -5/-0 and loses all abilities")
    void appliesReductionAndRemovesAbilities() {
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        Permanent aura = new Permanent(new QuantumReduction());
        aura.setAttachedTo(elves.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, elves).losesAllAbilities()).isTrue();
    }

    @Test
    @DisplayName("Teamwork lets Quantum Reduction be cast at instant speed")
    void teamworkAllowsInstantSpeedCast() {
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.stack.getLast().isTeamworkCostPaid()).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
    }

    @Test
    @DisplayName("Instant-speed casting requires paying teamwork")
    void instantSpeedCastingRequiresTeamwork() {
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must pay teamwork");
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
