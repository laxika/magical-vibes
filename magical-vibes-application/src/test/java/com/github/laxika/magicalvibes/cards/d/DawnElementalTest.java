package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnElemental.class, GrizzlyBears.class, Shock.class})
class DawnElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage that would be dealt to it")
    void preventsAllDamageToIt() {
        Permanent elemental = addCreatureReady(player1, new DawnElemental());
        harness.setHand(player2, List.of(new Shock()));

        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
    }

    @Test
    @DisplayName("Survives combat damage and still deals damage to a blocker")
    void survivesCombatAndDealsDamage() {
        Permanent elemental = addCreatureReady(player1, new DawnElemental());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        elemental.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(elemental);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }
}
