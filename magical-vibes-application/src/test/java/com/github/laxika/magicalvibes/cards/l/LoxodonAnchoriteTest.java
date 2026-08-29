package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonAnchoriteTest extends BaseCardTest {

    @Test
    void preventsTheNextTwoDamageToTargetCreature() {
        addReadyAnchorite(player1);
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void preventsTheNextTwoDamageToTargetPlayer() {
        addReadyAnchorite(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void targetPreventionShieldIsClearedAtEndOfTurn() {
        addReadyAnchorite(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.getOrDefault(player1.getId(), 0)).isZero();
    }

    private Permanent addReadyAnchorite(Player player) {
        Permanent anchorite = new Permanent(new LoxodonAnchorite());
        anchorite.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(anchorite);
        return anchorite;
    }
}
