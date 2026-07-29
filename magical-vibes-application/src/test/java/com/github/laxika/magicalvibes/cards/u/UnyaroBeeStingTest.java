package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UnyaroBeeStingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        GameData gd = harness.getGameData();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature, destroying a 2/2")
    void deals2DamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
