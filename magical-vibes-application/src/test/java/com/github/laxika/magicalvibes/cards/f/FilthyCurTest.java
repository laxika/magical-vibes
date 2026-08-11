package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FilthyCurTest extends BaseCardTest {

    @Test
    @DisplayName("Non-combat damage makes Filthy Cur's controller lose that much life")
    void nonCombatDamageMakesControllerLoseLife() {
        harness.addToBattlefield(player2, new FilthyCur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        UUID curId = harness.getPermanentId(player2, "Filthy Cur");
        harness.castInstant(player1, 0, curId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Filthy Cur");
    }

    @Test
    @DisplayName("Combat damage makes Filthy Cur's controller lose that much life")
    void combatDamageMakesControllerLoseLife() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new FilthyCur());
        harness.setLife(player2, 20);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent cur = gd.playerBattlefields.get(player2.getId()).getFirst();
        cur.setSummoningSick(false);
        cur.setBlocking(true);
        cur.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Filthy Cur");
    }
}
