package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IchorspitBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic 1 gives the defending player a poison counter on combat damage")
    void toxicDealsOnePoisonCounter() {
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new IchorspitBasilisk());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hillGiant = new Permanent(new HillGiant());
        hillGiant.setSummoningSick(false);
        hillGiant.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(hillGiant);

        Permanent basilisk = new Permanent(new IchorspitBasilisk());
        basilisk.setSummoningSick(false);
        basilisk.setBlocking(true);
        basilisk.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(basilisk);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Ichorspit Basilisk");
    }
}
