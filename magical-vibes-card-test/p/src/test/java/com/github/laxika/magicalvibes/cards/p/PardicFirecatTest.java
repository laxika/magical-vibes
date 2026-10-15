package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.y.YixlidJailer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PardicFirecat.class, FlameBurst.class, YixlidJailer.class})
class PardicFirecatTest extends BaseCardTest {

    @Test
    @DisplayName("Flame Burst counts Pardic Firecat in a graveyard")
    void flameBurstCountsPardicFirecatInGraveyard() {
        harness.setGraveyard(player1, List.of(new PardicFirecat()));
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Flame Burst counts Pardic Firecat in an opponent's graveyard")
    void flameBurstCountsPardicFirecatInOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new PardicFirecat()));
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pardic Firecat is not counted by Flame Burst outside a graveyard")
    void flameBurstDoesNotCountPardicFirecatOutsideGraveyard() {
        harness.addToBattlefield(player1, new PardicFirecat());
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Pardic Firecat can attack immediately")
    void pardicFirecatHasHaste() {
        Permanent firecat = harness.addToBattlefieldAndReturn(player1, new PardicFirecat());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThat(firecat.isAttacking()).isTrue();
    }
    @Test
    @DisplayName("Flame Burst counts every Firecat and Flame Burst in both graveyards")
    void countsMultipleFirecatsAlongsideFlameBursts() {
        harness.setGraveyard(player1, List.of(new PardicFirecat(), new FlameBurst()));
        harness.setGraveyard(player2, List.of(new PardicFirecat(), new PardicFirecat(), new FlameBurst()));
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Yixlid Jailer stops Firecat counting without stopping actual Flame Burst counting")
    void firecatDoesNotCountWhenGraveyardAbilitiesAreRemoved() {
        harness.addToBattlefield(player2, new YixlidJailer());
        harness.setGraveyard(player1, List.of(new PardicFirecat(), new FlameBurst()));
        harness.setGraveyard(player2, List.of(new PardicFirecat()));
        harness.setHand(player1, List.of(new FlameBurst()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
