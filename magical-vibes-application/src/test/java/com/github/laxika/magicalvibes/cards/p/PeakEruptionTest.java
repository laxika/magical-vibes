package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeakEruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target Mountain and deals 3 damage to its controller")
    void destroysMountainAndDealsDamageToItsController() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new PeakEruption()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Can target an opponent's Mountain")
    void canTargetOpponentsMountain() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new PeakEruption()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-Mountain land")
    void cannotTargetNonMountainLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new PeakEruption()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Mountain");
    }

    @Test
    @DisplayName("Fizzles if the target Mountain leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new PeakEruption()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Mountain");
        harness.castSorcery(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
