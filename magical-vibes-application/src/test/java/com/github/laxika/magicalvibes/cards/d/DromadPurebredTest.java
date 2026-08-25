package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DromadPurebred.class, FugitiveWizard.class, Shock.class})
class DromadPurebredTest extends BaseCardTest {

    @Test
    void gainsLifeWhenDealtNoncombatDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DromadPurebred());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID dromadId = harness.getPermanentId(player1, "Dromad Purebred");
        harness.castInstant(player2, 0, dromadId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Dromad Purebred");
    }

    @Test
    void gainsLifeWhenDealtCombatDamage() {
        harness.setLife(player1, 20);
        Permanent dromad = harness.addToBattlefieldAndReturn(player1, new DromadPurebred());
        dromad.setSummoningSick(false);
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent attacker = gd.playerBattlefields.get(player2.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        dromad.setBlocking(true);
        dromad.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Dromad Purebred");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }
}
