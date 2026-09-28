package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShinkaGatekeeper.class, Frostling.class})
class ShinkaGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Being dealt damage deals that much damage to its controller")
    void beingDealtDamageDamagesController() {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player2, new ShinkaGatekeeper());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, gatekeeper.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The trigger resolves after lethal damage removes it")
    void triggerResolvesAfterLethalDamageRemovesGatekeeper() {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player2, new ShinkaGatekeeper());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        harness.addToBattlefieldAndReturn(player1, new Frostling());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, gatekeeper.getId());
            resolveAllTriggers();
        }

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(gatekeeper);
    }
}
