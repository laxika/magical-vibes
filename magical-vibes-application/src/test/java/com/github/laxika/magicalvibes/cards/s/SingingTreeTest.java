package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SingingTree.class, GrizzlyBears.class})
class SingingTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Sets an attacking creature's base power to zero until end of turn")
    void setsAttackingCreatureBasePowerToZero() {
        Permanent tree = addCreatureReady(player1, new SingingTree());
        Permanent attacker = addAttacker(player2);

        activate(tree, attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base power set wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent tree = addCreatureReady(player1, new SingingTree());
        Permanent attacker = addAttacker(player2);

        activate(tree, attacker);
        assertThat(gqs.getEffectivePower(gd, attacker)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent tree = addCreatureReady(player1, new SingingTree());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tree.isTapped()).isFalse();
    }

    private void activate(Permanent tree, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = addCreatureReady(player, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }
}
