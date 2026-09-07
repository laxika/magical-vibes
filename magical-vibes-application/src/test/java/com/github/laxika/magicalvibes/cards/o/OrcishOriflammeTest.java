package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishOriflamme.class, GrizzlyBears.class})
class OrcishOriflammeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void buffsOwnAttackingCreatures() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff a non-attacking creature you control")
    void doesNotBuffNonAttackingCreatures() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's attacking creature")
    void doesNotBuffOpponentAttackers() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void bonusRemovedWhenCreatureStopsAttacking() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        bears.setAttacking(false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @CardUsed(Opalescence.class)
    @Test
    void animatedOriflammeAlsoBuffsItselfWhileAttacking() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent oriflamme = addCreatureReady(player1, new OrcishOriflamme());
        oriflamme.setAttacking(true);

        assertThat(gqs.isCreature(gd, oriflamme)).isTrue();
        assertThat(gqs.getEffectivePower(gd, oriflamme)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, oriflamme)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Orcish Oriflamme leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent oriflamme = harness.addToBattlefieldAndReturn(player1, new OrcishOriflamme());
        Permanent bears = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(oriflamme);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = addCreatureReady(controller, new GrizzlyBears());
        creature.setAttacking(true);
        return creature;
    }
}
