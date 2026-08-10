package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GrollubTest extends BaseCardTest {

    @Test
    @DisplayName("When Grollub is dealt damage, its opponent gains that much life")
    void opponentGainsDamageAmount() {
        harness.addToBattlefield(player1, new Grollub());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID grollubId = harness.getPermanentId(player1, "Grollub");
        harness.castInstant(player2, 0, grollubId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertOnBattlefield(player1, "Grollub");
    }
}
