package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfHope.class, Shock.class, GrizzlyBears.class})
class WallOfHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life equal to noncombat damage dealt to Wall of Hope")
    void gainsLifeFromNoncombatDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfHope());
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, wall.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller gains life equal to combat damage dealt to Wall of Hope")
    void gainsLifeFromCombatDamage() {
        harness.addToBattlefield(player2, new WallOfHope());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent wall = gd.playerBattlefields.get(player2.getId()).getFirst();
        wall.setSummoningSick(false);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);

        harness.setLife(player2, 10);
        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
    }
}
