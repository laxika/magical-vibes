package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SiltCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all lands its controller controls")
    void tapsAllLandsControllerControls() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        castAndResolve();

        assertThat(ownForest.isTapped()).isTrue();
        assertThat(ownIsland.isTapped()).isTrue();
        assertThat(ownBears.isTapped()).isFalse();
        assertThat(opponentForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves when its controller controls no lands")
    void resolvesWithNoLands() {
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Silt Crawler");
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SiltCrawler()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
