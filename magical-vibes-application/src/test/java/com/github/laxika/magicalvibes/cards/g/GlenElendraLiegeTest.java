package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlenElendraLiege.class, FugitiveWizard.class, BlackKnight.class, GrizzlyBears.class})
class GlenElendraLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other blue creatures you control")
    void buffsOtherBlue() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        Permanent blue = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        // 1/1 base + 1/1 = 2/2
        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs other black creatures you control")
    void buffsOtherBlack() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        Permanent black = harness.addToBattlefieldAndReturn(player1, new BlackKnight());

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());

        // Base 2/3, unaffected by its own "other" boosts
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff creatures that are neither blue nor black")
    void doesNotBuffOffColor() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        Permanent green = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's blue creatures")
    void doesNotBuffOpponent() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        Permanent opponentBlue = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());

        assertThat(gqs.getEffectivePower(gd, opponentBlue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBlue)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blue-and-black creature gets both boosts")
    void blueAndBlackGetsBothBoosts() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());
        harness.addToBattlefield(player1, new GlenElendraLiege());

        // Each Liege receives both bonuses from the other.

        // Base 2/3 + 1/1 (blue) + 1/1 (black) = 4/5
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(5);
    }

    @Test
    @DisplayName("Three Lieges each receive both bonuses from the other two")
    void multipleLiegesStackBothBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());

        for (Permanent liege : java.util.List.of(first, second, third)) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(7);
        }
    }

    @Test
    @DisplayName("Opposing Lieges do not grant either bonus to each other")
    void opposingLiegesDoNotBoostEachOther() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GlenElendraLiege());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GlenElendraLiege());

        for (Permanent liege : java.util.List.of(own, opponent)) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(3);
        }
    }
}
