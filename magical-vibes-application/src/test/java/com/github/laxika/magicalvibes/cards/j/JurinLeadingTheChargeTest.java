package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JurinLeadingTheCharge.class, GrizzlyBears.class})
class JurinLeadingTheChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Jurin boosts attacking creatures by the defending player's creature count")
    void boostsAttackingCreaturesByDefendingCreatureCount() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(jurin.getPowerModifier()).isEqualTo(2);
        assertThat(otherAttacker.getPowerModifier()).isEqualTo(2);
        assertThat(nonAttacker.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Jurin's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Jurin must be blocked if able")
    void mustBeBlockedIfAble() {
        Permanent jurin = addCreatureReady(player1, new JurinLeadingTheCharge());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        jurin.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }
}
