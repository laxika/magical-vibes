package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeeperOfProgenitusTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping a Mountain adds an additional red mana")
    void mountainProducesExtraRed() {
        harness.addToBattlefield(player1, new KeeperOfProgenitus());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        // 1 from Mountain + 1 additional from Keeper = 2
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a Forest adds an additional green mana")
    void forestProducesExtraGreen() {
        harness.addToBattlefield(player1, new KeeperOfProgenitus());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping a Plains adds an additional white mana")
    void plainsProducesExtraWhite() {
        harness.addToBattlefield(player1, new KeeperOfProgenitus());
        harness.addToBattlefield(player1, new Plains());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping an unlisted land type does not trigger the additional mana")
    void islandDoesNotTrigger() {
        harness.addToBattlefield(player1, new KeeperOfProgenitus());
        harness.addToBattlefield(player1, new Island());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Effect is symmetric — an opponent tapping a Mountain also gets the additional red")
    void opponentMountainAlsoBenefits() {
        harness.addToBattlefield(player1, new KeeperOfProgenitus());
        harness.addToBattlefield(player2, new Mountain());

        harness.tapPermanent(player2, 0);

        // The tapping player (player2) gets the additional {R}.
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Without Keeper in play a Mountain produces only one red")
    void noExtraWithoutKeeper() {
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }
}
