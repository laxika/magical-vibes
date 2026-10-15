package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    @CardUsed(Opalescence.class)
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

    @Test
    void multipleOriflammesApplyIndependentlyToEveryOwnAttacker() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        Permanent secondOriflamme = harness.addToBattlefieldAndReturn(player1, new OrcishOriflamme());
        Permanent firstAttacker = addAttackingBears(player1);
        Permanent secondAttacker = addAttackingBears(player1);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(secondOriflamme);

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(3);
    }

    @Test
    void unblockedAttackerDealsBoostedCombatDamage() {
        harness.addToBattlefield(player1, new OrcishOriflamme());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
