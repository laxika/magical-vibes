package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShinkaGatekeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Being dealt damage deals that much damage to its controller")
    void beingDealtDamageDamagesController() {
        harness.addToBattlefield(player2, new ShinkaGatekeeper());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent gatekeeper = findPermanent(player2, "Shinka Gatekeeper");
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player1, 0, gatekeeper.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
