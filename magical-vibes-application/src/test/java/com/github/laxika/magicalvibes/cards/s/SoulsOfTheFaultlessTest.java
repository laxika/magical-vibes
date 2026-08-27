package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SoulsOfTheFaultless.class, GrizzlyBears.class, Shock.class})
class SoulsOfTheFaultlessTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage causes its controller to gain life and the attacker to lose life")
    void gainsLifeAndAttackerLosesLifeFromCombatDamage() {
        harness.addToBattlefield(player2, new SoulsOfTheFaultless());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent souls = gd.playerBattlefields.get(player2.getId()).getFirst();
        souls.setSummoningSick(false);
        souls.setBlocking(true);
        souls.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertOnBattlefield(player2, "Souls of the Faultless");
    }

    @Test
    @DisplayName("Noncombat damage does not trigger Souls of the Faultless")
    void ignoresNoncombatDamage() {
        harness.addToBattlefield(player2, new SoulsOfTheFaultless());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Souls of the Faultless"));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }
}
