package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TorchCourier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmotronicWave.class, GrizzlyBears.class, TorchCourier.class})
class CosmotronicWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage only to opponents' creatures")
    void damagesOnlyOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCosmotronicWave();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponents' creatures can't block this turn")
    void preventsOpponentsCreaturesFromBlocking() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castCosmotronicWave();

        assertThat(bls.canBlockAttacker(gd, ownCreature, opposingCreature,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Kills each opposing one-toughness creature without damaging your creatures or players")
    void killsAllOpposingOneToughnessCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new TorchCourier());
        TorchCourier first = new TorchCourier();
        TorchCourier second = new TorchCourier();
        harness.addToBattlefield(player2, first);
        harness.addToBattlefield(player2, second);

        castCosmotronicWave();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creatures entering after resolution can't block even when no creatures were present")
    void preventsLaterCreaturesFromBlocking() {
        castCosmotronicWave();

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new TorchCourier());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new TorchCourier());

        assertThat(opposingCreature.getMarkedDamage()).isZero();
        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, ownCreature, opposingCreature,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at the end of the turn")
    void blockingRestrictionExpires() {
        castCosmotronicWave();
        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new TorchCourier());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new TorchCourier());
        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bls.canBlockAttacker(gd, opposingCreature, ownCreature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    private void castCosmotronicWave() {
        harness.castFromHand(player1, new CosmotronicWave(), "{3}{R}");
        harness.passBothPriorities();
    }
}
