package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinOriflamme.class, GrizzlyBears.class})
class GoblinOriflammeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void buffsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new GoblinOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff a non-attacking creature you control")
    void doesNotBuffNonAttackingCreatures() {
        harness.addToBattlefield(player1, new GoblinOriflamme());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's attacking creature")
    void doesNotBuffOpponentAttackers() {
        harness.addToBattlefield(player1, new GoblinOriflamme());
        Permanent bears = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void bonusIsRemovedWhenCreatureStopsAttacking() {
        harness.addToBattlefield(player1, new GoblinOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        bears.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }
}
