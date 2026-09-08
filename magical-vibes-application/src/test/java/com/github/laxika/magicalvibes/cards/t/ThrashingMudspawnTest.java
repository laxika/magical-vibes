package com.github.laxika.magicalvibes.cards.t;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrashingMudspawn.class, Shock.class, GrizzlyBears.class})
class ThrashingMudspawnTest extends BaseCardTest {

    @Test
    @DisplayName("When Thrashing Mudspawn is dealt spell damage, its controller loses that much life")
    void spellDamageCausesMatchingLifeLoss() {
        harness.addToBattlefield(player1, new ThrashingMudspawn());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        UUID mudspawnId = harness.getPermanentId(player1, "Thrashing Mudspawn");
        harness.castInstant(player2, 0, mudspawnId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Thrashing Mudspawn");
    }

    @Test
    @DisplayName("When Thrashing Mudspawn is dealt combat damage, its controller loses that much life")
    void combatDamageCausesMatchingLifeLoss() {
        harness.addToBattlefield(player1, new ThrashingMudspawn());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player2.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent mudspawn = gd.playerBattlefields.get(player1.getId()).getFirst();
        mudspawn.setSummoningSick(false);
        mudspawn.setBlocking(true);
        mudspawn.addBlockingTarget(0);

        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Thrashing Mudspawn");
    }
}
