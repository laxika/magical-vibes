package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FurySliver;
import com.github.laxika.magicalvibes.cards.t.TemporalEddy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MightSliver.class, FurySliver.class, AshcoatBear.class, TemporalEddy.class})
class MightSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Might Sliver boosts itself")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new FurySliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Boosts an opponent's Sliver too")
    void boostsOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new FurySliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Multiple Might Slivers stack")
    void multipleCopiesStack() {
        Permanent sliver = addCreatureReady(player2, new FurySliver());

        addCreatureReady(player1, new MightSliver());
        addCreatureReady(player1, new MightSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(7);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new MightSliver());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost ends for both players when Might Sliver leaves the battlefield")
    void boostEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new MightSliver());
        Permanent ownSliver = addCreatureReady(player1, new FurySliver());
        Permanent opposingSliver = addCreatureReady(player2, new FurySliver());

        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(5);

        harness.setHand(player1, List.of(new TemporalEddy()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, source.getId());

        harness.assertNotOnBattlefield(player1, "Might Sliver");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(source.getCard());
        assertThat(gqs.getEffectivePower(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSliver)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingSliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingSliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Might Sliver grants its boost only after resolving")
    void boostBeginsWhenSourceResolves() {
        Permanent sliver = addCreatureReady(player2, new FurySliver());

        harness.castFromHand(player1, new MightSliver(), "{4}{G}");

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Might Sliver");
        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(5);
    }
}
