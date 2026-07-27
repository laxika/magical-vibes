package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenFireTest extends BaseCardTest {

    @Test
    @DisplayName("Open Fire deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Open Fire deals 3 damage to target creature, destroying a 3/3")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new OpenFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
