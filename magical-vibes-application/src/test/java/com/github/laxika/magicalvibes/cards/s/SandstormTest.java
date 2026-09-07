package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sandstorm.class, BayFalcon.class, GiantMantis.class})
class SandstormTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each attacking creature")
    void deals1DamageToEachAttackingCreature() {
        harness.forceActivePlayer(player1);
        Permanent a1 = addAttacker(player1, player2, new GiantMantis());
        Permanent a2 = addAttacker(player1, player2, new GiantMantis());
        castSandstorm();

        assertThat(a1.getMarkedDamage()).isEqualTo(1);
        assertThat(a2.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Kills 1-toughness attacking creatures")
    void killsOneToughnessAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new BayFalcon());
        castSandstorm();

        harness.assertNotOnBattlefield(player1, "Bay Falcon");
        harness.assertInGraveyard(player1, "Bay Falcon");
    }

    @Test
    @DisplayName("Does not damage non-attacking creatures")
    void doesNotDamageNonAttackers() {
        harness.forceActivePlayer(player1);
        addAttacker(player1, player2, new BayFalcon());
        Permanent idle = addCreatureReady(player1, new GiantMantis());
        castSandstorm();

        assertThat(idle.getMarkedDamage()).isZero();
    }

    private void castSandstorm() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.castFromHand(player2, new Sandstorm(), "{G}");
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent perm = addCreatureReady(controller, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
