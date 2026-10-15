package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.m.MasterOfDiversion;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SteelformSliver.class, BonescytheSliver.class, MasterOfDiversion.class, DoomBlade.class})
class SteelformSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Steelform Sliver boosts itself (it is a Sliver)")
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new SteelformSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boosts another Sliver you control")
    void boostsOtherSliver() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new SteelformSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("Does not boost a non-Sliver creature")
    void doesNotBoostNonSliver() {
        addCreatureReady(player1, new SteelformSliver());
        Permanent nonSliver = addCreatureReady(player1, new MasterOfDiversion());

        assertThat(gqs.getEffectivePower(gd, nonSliver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonSliver)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's Sliver")
    void doesNotBoostOpponentSliver() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new SteelformSliver());

        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Multiple Steelform Slivers each boost themselves and other Slivers")
    void multipleCopiesStack() {
        Permanent first = addCreatureReady(player1, new SteelformSliver());
        Permanent second = addCreatureReady(player1, new SteelformSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());

        for (Permanent sliver : List.of(first, second, other)) {
            assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Destroying one boost source removes only its contribution")
    void destroyingSourceRemovesItsBoost() {
        Permanent first = addCreatureReady(player1, new SteelformSliver());
        Permanent second = addCreatureReady(player1, new SteelformSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        harness.assertInGraveyard(player1, "Steelform Sliver");
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }
}
