package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornetCobra.class, GrizzlyBears.class})
class HornetCobraTest extends BaseCardTest {

    @Test
    void firstStrikeKillsAOneOneBeforeItDealsCombatDamage() {
        HornetCobra cobra = new HornetCobra();
        Permanent attacker = new Permanent(cobra);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        GrizzlyBears bears = new GrizzlyBears();
        bears.setPower(1);
        bears.setToughness(1);
        Permanent blocker = new Permanent(bears);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
