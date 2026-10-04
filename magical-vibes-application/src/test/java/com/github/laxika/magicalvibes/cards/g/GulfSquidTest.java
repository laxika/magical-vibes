package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GulfSquid.class, Forest.class, PygmyRazorback.class})
class GulfSquidTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all lands target player controls")
    void tapsAllLandsTargetPlayerControls() {
        Permanent targetForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondTargetForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent targetBoar = harness.addToBattlefieldAndReturn(player2, new PygmyRazorback());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castAndResolve(player2.getId());

        assertThat(targetForest.isTapped()).isTrue();
        assertThat(secondTargetForest.isTapped()).isTrue();
        assertThat(targetBoar.isTapped()).isFalse();
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

    @Test
    @DisplayName("ETB waits for resolution and still taps newly added lands after the Squid leaves")
    void tapsCurrentLandsAfterSourceLeaves() {
        Permanent originalForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new GulfSquid()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, player2.getId());

        assertThat(originalForest.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Gulf Squid");
        assertThat(originalForest.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        Permanent squid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GulfSquid)
                .findFirst().orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, squid));
        Permanent newForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveAllTriggers();

        assertThat(originalForest.isTapped()).isTrue();
        assertThat(newForest.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Gulf Squid");
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GulfSquid()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, targetPlayerId);
        resolveAllTriggers();
    }
}
