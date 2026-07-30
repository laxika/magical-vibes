package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScrambleverseTest extends BaseCardTest {

    private void castScrambleverse() {
        harness.setHand(player1, List.of(new Scrambleverse()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private UUID controllerOf(UUID permanentId) {
        return gd.playerBattlefields.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(p -> p.getId().equals(permanentId)))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("Every nonland permanent ends up controlled by one of the players and untapped")
    void redistributesAndUntapsNonlands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        bears.tap();
        giant.tap();
        millstone.tap();

        castScrambleverse();

        for (Permanent permanent : List.of(bears, giant, millstone)) {
            assertThat(controllerOf(permanent.getId()))
                    .isIn(player1.getId(), player2.getId());
            assertThat(permanent.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Lands are untouched — controller and tapped state stay as they were")
    void landsAreUnaffected() {
        Permanent myForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent theirForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        theirForest.tap();

        castScrambleverse();

        assertThat(controllerOf(myForest.getId())).isEqualTo(player1.getId());
        assertThat(controllerOf(theirForest.getId())).isEqualTo(player2.getId());
        assertThat(theirForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Over many nonland permanents both players end up controlling some of them")
    void bothPlayersReceivePermanents() {
        for (int i = 0; i < 30; i++) {
            harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        }

        castScrambleverse();

        assertThat(gd.playerBattlefields.get(player1.getId())).isNotEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isNotEmpty();
    }
}
