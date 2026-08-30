package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtmosphereSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Atmosphere Surgeon")
    void noncreatureSpellPutsOilCounter() {
        Permanent surgeon = addSurgeonReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Atmosphere Surgeon")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent surgeon = addSurgeonReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removing an oil counter grants a target creature flying until end of turn")
    void removesOilCounterAndGrantsFlying() {
        Permanent surgeon = addSurgeonReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        surgeon.setCounterCount(CounterType.OIL, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying grant wears off at end of turn and activation is sorcery speed")
    void flyingGrantWearsOffAndActivationIsSorcerySpeed() {
        Permanent surgeon = addSurgeonReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        surgeon.setCounterCount(CounterType.OIL, 1);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    private Permanent addSurgeonReady(Player player) {
        return addCreatureReady(player, new AtmosphereSurgeon());
    }
}
