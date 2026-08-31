package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.s.StormShaman;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HailStorm.class, AesthirGlider.class, StormShaman.class})
class HailStormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each attacking creature")
    void damagesAttackingCreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        castHailStorm();

        harness.assertNotOnBattlefield(player1, "Aesthir Glider");
    }

    @Test
    @DisplayName("Deals exactly 2 damage to an attacking creature the caster does not control")
    void damagesOpponentAttackerOnlyOnce() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttacker(player1, player2, new StormShaman());
        castHailStorm();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Storm Shaman");
    }

    @Test
    @DisplayName("Non-attacking creatures the caster does not control are untouched")
    void doesNotDamageOpponentsNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player1, new StormShaman());
        castHailStorm();

        harness.assertOnBattlefield(player1, "Storm Shaman");
        assertThat(findPermanent(player1, "Storm Shaman").getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals 1 damage to the caster and each creature they control")
    void damagesCasterAndTheirCreatures() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2);
        harness.addToBattlefield(player2, new StormShaman());
        int lifeBefore = gd.getLife(player2.getId());
        int attackerLifeBefore = gd.getLife(player1.getId());

        castHailStorm();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(attackerLifeBefore);
        assertThat(findPermanent(player2, "Storm Shaman").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An attacking creature the caster controls takes both 2 and 1 damage")
    void casterAttackerTakesThreeDamage() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addAttacker(player2, player1, new StormShaman());

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
        return addAttacker(attackerController, defender, new AesthirGlider());
    }

    private Permanent addAttacker(Player attackerController, Player defender, Card card) {
        Permanent perm = addCreatureReady(attackerController, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
