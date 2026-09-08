package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GulfSquidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all lands target player controls")
    void tapsAllLandsTargetPlayerControls() {
        Permanent targetForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent targetBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAndResolve(player2.getId());

        assertThat(targetForest.isTapped()).isTrue();
        assertThat(targetBears.isTapped()).isFalse();
        assertThat(ownForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may target themselves")
    void mayTargetController() {
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAndResolve(player1.getId());

        assertThat(ownForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB resolves with no lands to tap")
    void resolvesWithNoLands() {
        castAndResolve(player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Gulf Squid");
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GulfSquid()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
