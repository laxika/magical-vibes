package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NaturesRevolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanAdvocate.class, Forest.class, GrizzlyBears.class, NaturesRevolt.class})
class SylvanAdvocateTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts itself and land creatures you control with six or more lands")
    void boostsSelfAndControlledLandCreaturesAtThreshold() {
        Permanent advocate = harness.addToBattlefieldAndReturn(player1, new SylvanAdvocate());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        addLands(player1, 6);
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent ownLand = findPermanent(player1, "Forest");

        assertThat(gqs.getEffectivePower(gd, advocate)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, advocate)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownLand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownLand)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentLand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost with fewer than six lands")
    void doesNotBoostBelowThreshold() {
        Permanent advocate = harness.addToBattlefieldAndReturn(player1, new SylvanAdvocate());
        addLands(player1, 5);
        harness.addToBattlefield(player1, new NaturesRevolt());

        Permanent ownLand = findPermanent(player1, "Forest");

        assertThat(gqs.getEffectivePower(gd, advocate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, advocate)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownLand)).isEqualTo(2);
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }
}
