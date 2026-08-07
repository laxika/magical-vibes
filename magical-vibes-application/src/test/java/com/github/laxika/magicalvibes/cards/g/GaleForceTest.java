package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GaleForceTest extends BaseCardTest {

    private void castGaleForce() {
        harness.setHand(player1, List.of(new GaleForce()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gale Force kills flying creatures on both sides")
    void killsFlyers() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player2, new SerraAngel());

        castGaleForce();

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Gale Force leaves non-flying creatures alone")
    void sparesNonFlyers() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castGaleForce();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Gale Force deals no damage to players")
    void dealsNoDamageToPlayers() {
        castGaleForce();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
