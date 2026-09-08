package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MerfolkMistbinderTest extends BaseCardTest {

    @Test
    @DisplayName("Other Merfolk you control get +1/+1")
    void buffsOtherMerfolkYouControl() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        harness.addToBattlefield(player1, new CoralMerfolk());

        Permanent merfolk = findPermanent(player1, "Coral Merfolk");

        assertThat(gqs.getEffectivePower(gd, merfolk)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, merfolk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Merfolk Mistbinder does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());

        Permanent mistbinder = findPermanent(player1, "Merfolk Mistbinder");

        assertThat(gqs.getEffectivePower(gd, mistbinder)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mistbinder)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Merfolk creatures")
    void doesNotBuffNonMerfolk() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff an opponent's Merfolk")
    void doesNotBuffOpponentsMerfolk() {
        harness.addToBattlefield(player1, new MerfolkMistbinder());
        harness.addToBattlefield(player2, new CoralMerfolk());

        Permanent opponentMerfolk = findPermanent(player2, "Coral Merfolk");

        assertThat(gqs.getEffectivePower(gd, opponentMerfolk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentMerfolk)).isEqualTo(1);
    }
}
