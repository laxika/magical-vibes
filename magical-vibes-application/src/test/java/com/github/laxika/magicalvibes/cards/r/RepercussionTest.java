package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepercussionTest extends BaseCardTest {

    @Test
    @DisplayName("Reflects noncombat damage to the damaged creature's controller")
    void reflectsNoncombatDamage() {
        harness.addToBattlefield(player1, new Repercussion());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Still damages the controller when the damaged creature dies")
    void reflectsLethalDamage() {
        harness.addToBattlefield(player1, new Repercussion());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Reflects combat damage dealt to each damaged creature's controller")
    void reflectsCombatDamage() {
        harness.addToBattlefield(player1, new Repercussion());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = findPermanent(player2, "Hill Giant");
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
