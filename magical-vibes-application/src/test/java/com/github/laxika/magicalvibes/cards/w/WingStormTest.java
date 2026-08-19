package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WingStormTest extends BaseCardTest {

    private void castWingStorm() {
        harness.setHand(player1, List.of(new WingStorm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals twice the number of flying creatures to each player")
    void dealsTwiceFlyingCreatureCountToEachPlayer() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ignores creatures without flying")
    void ignoresCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castWingStorm();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
