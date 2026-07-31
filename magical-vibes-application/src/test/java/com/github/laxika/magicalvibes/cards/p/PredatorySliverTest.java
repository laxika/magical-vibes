package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PredatorySliverTest extends BaseCardTest {

    @Test
    @DisplayName("Predatory Sliver buffs itself")
    void buffsItself() {
        harness.addToBattlefield(player1, new PredatorySliver());

        Permanent sliver = findPermanent(player1, "Predatory Sliver");

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
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff opponent's Slivers")
    void doesNotBuffOpponentSlivers() {
        harness.addToBattlefield(player1, new PredatorySliver());
        harness.addToBattlefield(player2, new PredatorySliver());

        Permanent opponentSliver = findPermanent(player2, "Predatory Sliver");

        // Only boosted by its own controller's copy (itself), not by player1's.
        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Predatory Sliver resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new PredatorySliver());

        Permanent sliver = findPermanent(player1, "Predatory Sliver");
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
}
