package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HailStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature")
    void damagesAttackingCreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        castHailStorm();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-attacking creatures the caster does not control are untouched")
    void doesNotDamageOpponentsNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player1, new GiantSpider());
        castHailStorm();

        harness.assertOnBattlefield(player1, "Giant Spider");
        assertThat(findPermanent(player1, "Giant Spider").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals 1 damage to the caster and each creature they control")
    void damagesCasterAndTheirCreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player2, new GiantSpider());
        int lifeBefore = gd.getLife(player2.getId());
        int attackerLifeBefore = gd.getLife(player1.getId());

        castHailStorm();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore);
        assertThat(findPermanent(player2, "Giant Spider").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An attacking creature the caster controls takes both 2 and 1 damage")
    void casterAttackerTakesThreeDamage() {
        harness.forceActivePlayer(player2);
        Permanent attacker = new Permanent(new GiantSpider());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        castHailStorm();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    private void castHailStorm() {
        harness.setHand(player2, List.of(new HailStorm()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttacker(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }
}
