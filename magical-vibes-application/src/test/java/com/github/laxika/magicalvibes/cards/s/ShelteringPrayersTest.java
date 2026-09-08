package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShelteringPrayersTest extends BaseCardTest {

    @Test
    void grantsShroudToBasicLandsWhenTheirControllerHasThreeOrFewerLands() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent firstPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondPlains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent thirdPlains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, firstPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondPlains, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, thirdPlains, Keyword.SHROUD)).isTrue();
    }

    @Test
    void countsEachPlayersLandsIndependentlyAndAffectsBasicLandsOnly() {
        harness.addToBattlefield(player1, new ShelteringPrayers());
        Permanent ownBasicLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Wasteland());

        Permanent opponentBasicLand = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent opponentNonbasicLand = harness.addToBattlefieldAndReturn(player2, new Wasteland());

        assertThat(gqs.hasKeyword(gd, ownBasicLand, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBasicLand, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentNonbasicLand, Keyword.SHROUD)).isFalse();
    }
}
