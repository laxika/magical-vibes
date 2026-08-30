package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloryBearers.class, GrizzlyBears.class})
class GloryBearersTest extends BaseCardTest {

    @Test
    @DisplayName("Another attacking creature gets +0/+1 until end of turn")
    void anotherAttackingCreatureGetsToughnessBoost() {
        addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Glory Bearers does not boost itself when it attacks")
    void doesNotBoostItself() {
        Permanent gloryBearers = addCreatureReady(player1, new GloryBearers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gloryBearers.getPowerModifier()).isEqualTo(0);
        assertThat(gloryBearers.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each other attacking creature gets its own boost")
    void boostsEachOtherAttacker() {
        addCreatureReady(player1, new GloryBearers());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(firstAttacker.getToughnessModifier()).isEqualTo(1);
        assertThat(secondAttacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new GloryBearers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }
}
