package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImperialCeratopsTest extends BaseCardTest {

    @Test
    void spellDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new ImperialCeratops());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        UUID ceratopsId = harness.getPermanentId(player2, "Imperial Ceratops");
        harness.castInstant(player1, 0, ceratopsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Imperial Ceratops");
    }

    @Test
    void combatDamageTriggersEnrage() {
        harness.addToBattlefield(player2, new ImperialCeratops());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent ceratops = gd.playerBattlefields.get(player2.getId()).getFirst();
        ceratops.setSummoningSick(false);
        ceratops.setBlocking(true);
        ceratops.addBlockingTarget(0);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player2, "Imperial Ceratops");
    }
}
