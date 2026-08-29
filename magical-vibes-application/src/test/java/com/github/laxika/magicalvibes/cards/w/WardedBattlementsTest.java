package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WardedBattlementsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control get +1/+0")
    void buffsOwnAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures you control are unaffected")
    void doesNotAffectNonAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's attacking creatures are unaffected")
    void doesNotAffectOpponentAttackers() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player2);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The bonus is removed when Warded Battlements leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new WardedBattlements());
        Permanent attacker = addAttackingBears(player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Warded Battlements"));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    private Permanent addAttackingBears(Player controller) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(controller.getId()).add(creature);
        return creature;
    }
}
