package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.n.NovaCleric;
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

@CardUsed({DauntingDefender.class, ElvishWarrior.class, NovaCleric.class, Shock.class})
class DauntingDefenderTest extends BaseCardTest {

    @Test
    void preventsOneDamageFromNoncombatSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void preventsOneDamageFromEachSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());

        castShock(player2, defender.getId());
        castShock(player2, defender.getId());

        assertThat(defender.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void preventsDamageToAnotherClericYouControl() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new NovaCleric());

        castShock(player2, cleric.getId());

        assertThat(cleric.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void doesNotPreventDamageToNonClericsOrPlayers() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        int lifeBefore = gd.getLife(player1.getId());

        castShock(player2, elf.getId());
        castShock(player2, player1.getId());

        assertThat(elf.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void doesNotPreventDamageToAnOpponentsCleric() {
        harness.addToBattlefield(player1, new DauntingDefender());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new NovaCleric());

        castShock(player1, opponentCleric.getId());

        assertThat(opponentCleric.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCleric);
    }

    @Test
    void preventsOneCombatDamageFromEachSourceToClericYouControl() {
        Permanent defender = harness.addToBattlefieldAndReturn(player1, new DauntingDefender());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
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
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
