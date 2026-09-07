package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntingDefender.class, GrizzlyBears.class, Shock.class})
class DauntingDefenderTest extends BaseCardTest {

    @Test
    void preventsOneDamageFromNoncombatSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotPreventDamageToNonClericsOrPlayers() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        castShock(player2, bear.getId());
        castShock(player2, player1.getId());

        assertThat(bear.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void preventsOneCombatDamageFromEachSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        defender.setSummoningSick(false);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    private void castShock(Player player, UUID targetId) {
        harness.setHand(player, List.of(new Shock()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castInstant(player, 0, targetId);
        harness.passBothPriorities();
    }
}
