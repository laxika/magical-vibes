package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TempestDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Base stats are 0/4 with no basic Islands")
    void baseStatsWithNoIslands() {
        harness.addToBattlefield(player1, new TempestDjinn());

        Permanent djinn = findPermanent(player1, "Tempest Djinn");
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each basic Island you control")
    void boostsWithBasicIslands() {
        harness.addToBattlefield(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        Permanent djinn = findPermanent(player1, "Tempest Djinn");
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's basic Islands do not contribute to the bonus")
    void opponentIslandsDontCount() {
        harness.addToBattlefield(player1, new TempestDjinn());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent djinn = findPermanent(player1, "Tempest Djinn");
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Non-Island basic lands do not contribute to the bonus")
    void nonIslandLandsDontCount() {
        harness.addToBattlefield(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        Permanent djinn = findPermanent(player1, "Tempest Djinn");
        // Only 1 basic Island, Forest does not count
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus updates when a basic Island leaves the battlefield")
    void bonusUpdatesWhenIslandLeaves() {
        harness.addToBattlefield(player1, new TempestDjinn());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        Permanent djinn = findPermanent(player1, "Tempest Djinn");
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Island"));

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
