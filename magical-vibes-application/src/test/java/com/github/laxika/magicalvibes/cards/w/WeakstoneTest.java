package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Weakstone.class, GrizzlyBears.class})
class WeakstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures get -1/-0")
    void weakensAttackingCreatures() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent attacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonattacking creatures are unaffected")
    void ignoresNonattackingCreatures() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creatures on either side are weakened")
    void weakensAttackingCreaturesRegardlessOfController() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent ownAttacker = addAttacker(player1, player2);
        Permanent opponentAttacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(1);
    }

    private Permanent addAttacker(Player controller, Player attackTarget) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget.getId());
        gd.playerBattlefields.get(controller.getId()).add(attacker);
        return attacker;
    }
}
