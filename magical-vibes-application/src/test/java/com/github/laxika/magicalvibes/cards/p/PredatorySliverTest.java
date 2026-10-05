package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SentinelSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatorySliver.class, GrizzlyBears.class, SentinelSliver.class})
class PredatorySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Predatory Sliver buffs itself")
    void buffsItself() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new PredatorySliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Predatory Slivers stack into 3/3s")
    void twoSliversStack() {
        harness.addToBattlefield(player1, new PredatorySliver());
        harness.addToBattlefield(player1, new PredatorySliver());

        List<Permanent> slivers = findPermanents(player1, "Predatory Sliver");

        assertThat(slivers).hasSize(2);
        for (Permanent sliver : slivers) {
            assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Does not buff non-Sliver creatures you control")
    void doesNotBuffNonSlivers() {
        harness.addToBattlefield(player1, new PredatorySliver());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Slivers")
    void doesNotBuffOpponentSlivers() {
        harness.addToBattlefield(player1, new PredatorySliver());
        Permanent opponentSliver = harness.addToBattlefieldAndReturn(player2, new PredatorySliver());

        // Only boosted by its own controller's copy (itself), not by player1's.
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Predatory Sliver resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new PredatorySliver());
        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);

        harness.setHand(player1, List.of(new PredatorySliver()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when the source leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new PredatorySliver());
        harness.addToBattlefield(player1, new PredatorySliver());

        List<Permanent> slivers = findPermanents(player1, "Predatory Sliver");
        Permanent survivor = slivers.get(0);
        Permanent leaving = slivers.get(1);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(leaving);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs another Sliver you control but not an opponent's Sliver")
    void buffsDifferentSliverOnlyForController() {
        harness.addToBattlefield(player1, new PredatorySliver());
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new SentinelSliver());
        Permanent opponentSliver = harness.addToBattlefieldAndReturn(player2, new SentinelSliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Predatory Sliver does not boost creatures while it is on the stack")
    void bonusStartsOnlyAfterResolution() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new SentinelSliver());
        harness.setHand(player1, List.of(new PredatorySliver()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }
}
