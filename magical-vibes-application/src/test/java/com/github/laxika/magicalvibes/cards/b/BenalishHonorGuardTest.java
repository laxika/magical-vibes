package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BenalishHonorGuardTest extends BaseCardTest {

    // ===== Static effect: +1/+0 per legendary creature you control =====

    @Test
    @DisplayName("Base stats are 2/2 with no legendary creatures")
    void baseStatsWithNoLegendaryCreatures() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +1/+0 for one legendary creature you control")
    void boostsWithOneLegendaryCreature() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());
        harness.addToBattlefield(player1, new BairdStewardOfArgive());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        // 2/2 base + 1/0 from Baird = 3/2
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+0 for two legendary creatures you control")
    void boostsWithTwoLegendaryCreatures() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());
        harness.addToBattlefield(player1, new BairdStewardOfArgive());
        harness.addToBattlefield(player1, new BairdStewardOfArgive());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        // 2/2 base + 2/0 from two legendary creatures = 4/2
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-legendary creatures do not contribute to the bonus")
    void nonLegendaryCreaturesDontCount() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's legendary creatures do not contribute to the bonus")
    void opponentLegendaryCreaturesDontCount() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());
        harness.addToBattlefield(player2, new BairdStewardOfArgive());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus is removed when legendary creature leaves the battlefield")
    void bonusRemovedWhenLegendaryLeaves() {
        harness.addToBattlefield(player1, new BenalishHonorGuard());
        harness.addToBattlefield(player1, new BairdStewardOfArgive());

        Permanent guard = findPermanent(player1, "Benalish Honor Guard");
        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Baird, Steward of Argive"));

        assertThat(gqs.getEffectivePower(gd, guard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, guard)).isEqualTo(2);
    }

    // ===== Helpers =====

}
