package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Desert.class, GrizzlyBears.class})
class DesertTest extends BaseCardTest {

    @Test
    @DisplayName("Adds colorless mana")
    void addsColorlessMana() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());

        harness.activateAbility(player1, indexOf(player1, desert), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to an attacking creature during end of combat")
    void damagesAttackingCreatureDuringEndOfCombat() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.activateAbility(player1, indexOf(player1, desert), 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, desert), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the damage ability outside end of combat")
    void cannotActivateOutsideEndOfCombat() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, desert), 1, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("end of combat step");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
