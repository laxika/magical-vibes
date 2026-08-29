package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Sabotage Strategist")
class SabotageStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Shrinks creatures attacking its controller")
    void shrinksAttackingCreatures() {
        harness.addToBattlefield(player1, new SabotageStrategist());
        Permanent attacker1 = addReadyAttacker();
        Permanent attacker2 = addReadyAttacker();

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, attacker1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, attacker2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust puts three +1/+1 counters on it and can be used only once")
    void exhaustAbility() {
        Permanent strategist = addReadyStrategist();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(strategist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        addExhaustMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    private Permanent addReadyAttacker() {
        return addCreatureReady(player2, new GrizzlyBears());
    }

    private Permanent addReadyStrategist() {
        return addCreatureReady(player1, new SabotageStrategist());
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
