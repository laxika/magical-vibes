package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mobilize.class, GrizzlyBears.class, Island.class})
class MobilizeTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all tapped creatures you control")
    void untapsAllTappedCreaturesYouControl() {
        Permanent bear1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear1.tap();
        Permanent bear2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear2.tap();

        harness.castFromHand(player1, new Mobilize(), "{G}");
        harness.passBothPriorities();

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBear.tap();

        harness.castFromHand(player1, new Mobilize(), "{G}");
        harness.passBothPriorities();

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents you control")
    void doesNotUntapNonCreaturePermanents() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        harness.castFromHand(player1, new Mobilize(), "{G}");
        harness.passBothPriorities();

        assertThat(island.isTapped()).isTrue();
    }
}
