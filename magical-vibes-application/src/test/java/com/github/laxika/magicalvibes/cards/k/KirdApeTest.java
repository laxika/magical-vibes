package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Taiga;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KirdApe.class, Forest.class, Taiga.class})
class KirdApeTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+2 when controller controls a Forest")
    void boostedWithForest() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2); // 1 base + 1
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3); // 1 base + 2
    }

    @Test
    @DisplayName("A nonbasic land with the Forest subtype grants the bonus")
    void boostedWithNonbasicForest() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        harness.addToBattlefield(player1, new Taiga());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3);
    }

    @Test
    @DisplayName("No bonus without a Forest")
    void noBonusWithoutForest() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Forest does not grant bonus")
    void opponentForestDoesNotCount() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses bonus when the Forest leaves the battlefield")
    void losesBonusWhenForestLeaves() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, forest));

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains the bonus immediately when a Forest enters later")
    void gainsBonusWhenForestEnters() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Forests grant only one bonus, which persists until the last leaves")
    void multipleForestsDoNotStack() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstForest));

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondForest));

        assertThat(gqs.getEffectivePower(gd, ape)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ape)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Forest still grants the bonus only to its controller's Ape")
    void tappedForestBoostsOnlyControllersApe() {
        Permanent ownApe = harness.addToBattlefieldAndReturn(player1, new KirdApe());
        Permanent opposingApe = harness.addToBattlefieldAndReturn(player2, new KirdApe());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setTapped(true);

        assertThat(gqs.getEffectivePower(gd, ownApe)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownApe)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingApe)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingApe)).isEqualTo(1);
    }
}
