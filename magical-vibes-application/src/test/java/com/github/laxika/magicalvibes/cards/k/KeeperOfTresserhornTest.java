package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeeperOfTresserhornTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = new Permanent(new KeeperOfTresserhorn());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atk);
        return atk;
    }

    private void declareBlockers(List<BlockerAssignment> assignments) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, assignments);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unblocked: defending player loses 2 life and the attacker deals no combat damage")
    void unblockedDrainsTwoAndDealsNoDamage() {
        Permanent attacker = addAttacker();
        int startingLife = gd.getLife(player2.getId());

        declareBlockers(List.of());

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The 6/6 assigns no combat damage, so only the 2 life loss happened.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("Blocked: no life loss and normal combat damage is dealt")
    void blockedDoesNotTrigger() {
        Permanent attacker = addAttacker();
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        int startingLife = gd.getLife(player2.getId());

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareBlockers(List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }
}
