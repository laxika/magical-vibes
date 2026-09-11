package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdOfTrueBelievers.class, GrizzlyBears.class})
class CrowdOfTrueBelieversTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature attacking alone and gains 1 life")
    void boostsAttackingAloneCreatureAndGainsLife() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot target a creature when multiple creatures were declared as attackers")
    void cannotTargetCreatureWhenAttackingWithMultipleCreatures() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1, 2));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking alone");
    }
}
