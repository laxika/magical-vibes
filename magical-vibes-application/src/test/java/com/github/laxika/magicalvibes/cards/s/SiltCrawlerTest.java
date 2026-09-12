package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SiltCrawler.class, RhysticCave.class, PygmyRazorback.class})
class SiltCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all lands its controller controls")
    void tapsAllLandsControllerControls() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent ownOtherLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PygmyRazorback());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new RhysticCave());

        castAndResolve();

        assertThat(ownLand.isTapped()).isTrue();
        assertThat(ownOtherLand.isTapped()).isTrue();
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves when its controller controls no lands")
    void resolvesWithNoLands() {
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Silt Crawler");
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new SiltCrawler(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
