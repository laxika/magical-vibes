package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scarecrow.class, WindDrake.class, GrizzlyBears.class})
class ScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from flying creatures but not nonflying creatures")
    void preventsDamageFromFlyingCreaturesOnly() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new Scarecrow());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addAttacker(player2, new WindDrake());
        addAttacker(player2, new GrizzlyBears());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage before the ability resolves")
    void doesNotPreventDamageBeforeActivation() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Scarecrow());
        addAttacker(player2, new WindDrake());

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
