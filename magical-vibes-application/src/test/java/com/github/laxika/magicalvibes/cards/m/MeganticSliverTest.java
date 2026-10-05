package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeganticSliver.class, BonescytheSliver.class, GrizzlyBears.class, DoomBlade.class})
class MeganticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Megantic Sliver boosts itself (it is a Sliver)")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new MeganticSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new MeganticSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 3);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new MeganticSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Sliver")
    void doesNotBoostOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);

        addCreatureReady(player1, new MeganticSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower);
    }

    @Test
    @DisplayName("Multiple Megantic Slivers each boost themselves and other Slivers")
    void multipleBoostsStack() {
        Permanent first = addCreatureReady(player1, new MeganticSliver());
        Permanent second = addCreatureReady(player1, new MeganticSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(8);
    }

    @Test
    @DisplayName("Destroying Megantic Sliver immediately removes its boost")
    void boostEndsWhenSourceLeaves() {
        Permanent source = addCreatureReady(player1, new MeganticSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, source.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Sliver entering later immediately receives the boost")
    void boostsSliverEnteringLater() {
        addCreatureReady(player1, new MeganticSliver());
        harness.castFromHand(player1, new BonescytheSliver(), "{3}{W}");
        harness.passBothPriorities();

        Permanent other = findPermanent(player1, "Bonescythe Sliver");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
    }
}
