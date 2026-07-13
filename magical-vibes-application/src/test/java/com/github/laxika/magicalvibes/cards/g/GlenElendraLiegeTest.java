package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlenElendraLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other blue creatures you control")
    void buffsOtherBlue() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent blue = findPermanent(player1, "Fugitive Wizard");

        // 1/1 base + 1/1 = 2/2
        assertThat(gqs.getEffectivePower(gd, blue)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blue)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs other black creatures you control")
    void buffsOtherBlack() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent black = findPermanent(player1, "Black Knight");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new GlenElendraLiege());

        Permanent liege = findPermanent(player1, "Glen Elendra Liege");

        // Base 2/3, unaffected by its own "other" boosts
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff creatures that are neither blue nor black")
    void doesNotBuffOffColor() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent green = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's blue creatures")
    void doesNotBuffOpponent() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        harness.addToBattlefield(player2, new FugitiveWizard());

        Permanent opponentBlue = findPermanent(player2, "Fugitive Wizard");

        assertThat(gqs.getEffectivePower(gd, opponentBlue)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBlue)).isEqualTo(1);
    }

    @Test
    @DisplayName("A blue-and-black creature gets both boosts")
    void blueAndBlackGetsBothBoosts() {
        harness.addToBattlefield(player1, new GlenElendraLiege());
        harness.addToBattlefield(player1, new GlenElendraLiege());

        // The second Liege is both blue and black ({U/B} hybrid), so it receives +1/+1 twice from the first.
        Permanent boosted = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glen Elendra Liege"))
                .findFirst().orElseThrow();

        // Base 2/3 + 1/1 (blue) + 1/1 (black) = 4/5
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, boosted)).isEqualTo(5);
    }
}
