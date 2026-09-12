package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearingWind.class, DivingGriffin.class})
class SearingWindTest extends BaseCardTest {

    @Test
    @DisplayName("Searing Wind deals 10 damage to target player")
    void deals10DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Searing Wind deals 10 damage to target creature, destroying a 2/2")
    void deals10DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new DivingGriffin());
        harness.setHand(player1, List.of(new SearingWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        UUID targetId = harness.getPermanentId(player2, "Diving Griffin");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Diving Griffin");
        harness.assertInGraveyard(player2, "Diving Griffin");
    }
}
