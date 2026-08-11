package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PardicFirecatTest extends BaseCardTest {

    @Test
    @DisplayName("Flame Burst counts Pardic Firecat in a graveyard")
    void flameBurstCountsPardicFirecatInGraveyard() {
        gd.playerGraveyards.get(player1.getId()).add(new PardicFirecat());
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pardic Firecat is not counted by Flame Burst outside a graveyard")
    void flameBurstDoesNotCountPardicFirecatOutsideGraveyard() {
        harness.addToBattlefield(player1, new PardicFirecat());
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Pardic Firecat can attack immediately")
    void pardicFirecatHasHaste() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new PardicFirecat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(firecat.isAttacking()).isTrue();
    }
}
