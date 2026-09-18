package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DwarvenScorcher;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.t.TunnelerWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenScorcher.class, GiantWarthog.class, SuntailHawk.class, SwirlingSandstorm.class, TunnelerWurm.class})
class SwirlingSandstormTest extends BaseCardTest {

    @Test
    @DisplayName("Without threshold, Swirling Sandstorm deals no damage even if an opponent has threshold")
    void doesNothingBelowThreshold() {
        Permanent nonFlyingCreature = harness.addToBattlefieldAndReturn(player1, new TunnelerWurm());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setGraveyard(player1, graveyardWithTunnelerWurms(6));
        harness.setGraveyard(player2, graveyardWithTunnelerWurms(7));

        castAndResolve();

        assertThat(nonFlyingCreature.getMarkedDamage()).isZero();
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With threshold, Swirling Sandstorm deals exactly 5 damage to every non-flying creature")
    void thresholdDamagesNonFlyingCreaturesOnly() {
        Permanent ownNonFlyingCreature = harness.addToBattlefieldAndReturn(player1, new TunnelerWurm());
        Permanent opposingNonFlyingCreature = harness.addToBattlefieldAndReturn(player2, new TunnelerWurm());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setGraveyard(player1, graveyardWithTunnelerWurms(7));

        castAndResolve();

        assertThat(ownNonFlyingCreature.getMarkedDamage()).isEqualTo(5);
        assertThat(opposingNonFlyingCreature.getMarkedDamage()).isEqualTo(5);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");
        harness.passBothPriorities();
    }

    private List<Card> graveyardWithTunnelerWurms(int count) {
        List<Card> cards = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            cards.add(new TunnelerWurm());
        }
        return cards;
    }

    @Test
    @DisplayName("With threshold, Swirling Sandstorm deals exactly 5 damage to non-flying creatures")
    void thresholdDealsExactlyFiveToNonFlyingCreatures() {
        harness.addToBattlefield(player1, new GiantWarthog());
        harness.addToBattlefield(player2, new GiantWarthog());
        harness.addToBattlefield(player2, new TunnelerWurm());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new SwirlingSandstorm()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Warthog");
        harness.assertNotOnBattlefield(player2, "Giant Warthog");
        harness.assertOnBattlefield(player2, "Tunneler Wurm");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Threshold uses only the caster's graveyard")
    void thresholdUsesCastersGraveyardOnly() {
        harness.addToBattlefield(player1, new DwarvenScorcher());
        harness.addToBattlefield(player2, new DwarvenScorcher());
        harness.setGraveyard(player1, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher()));
        harness.setGraveyard(player2, List.of(
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher(), new DwarvenScorcher(), new DwarvenScorcher(),
                new DwarvenScorcher()));
        harness.castFromHand(player1, new SwirlingSandstorm(), "{3}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Dwarven Scorcher");
    }
}
