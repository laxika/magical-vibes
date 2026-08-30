package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliverLegion.class, MetallicSliver.class, GrizzlyBears.class})
class SliverLegionTest extends BaseCardTest {

    @Test
    @DisplayName("Sliver creatures get +1/+1 for each other Sliver on the battlefield")
    void boostsSliversByOtherSlivers() {
        Permanent firstSliver = harness.addToBattlefieldAndReturn(player1, new MetallicSliver());
        Permanent secondSliver = harness.addToBattlefieldAndReturn(player2, new MetallicSliver());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int firstBasePower = firstSliver.getEffectivePower();
        int firstBaseToughness = firstSliver.getEffectiveToughness();
        int secondBasePower = secondSliver.getEffectivePower();
        int secondBaseToughness = secondSliver.getEffectiveToughness();
        int unrelatedBasePower = unrelatedCreature.getEffectivePower();
        int unrelatedBaseToughness = unrelatedCreature.getEffectiveToughness();
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        int legionBasePower = legion.getCard().getPower();
        int legionBaseToughness = legion.getCard().getToughness();

        assertThat(gqs.getEffectivePower(gd, firstSliver)).isEqualTo(firstBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, firstSliver)).isEqualTo(firstBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, secondSliver)).isEqualTo(secondBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, secondSliver)).isEqualTo(secondBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, unrelatedCreature)).isEqualTo(unrelatedBasePower);
        assertThat(gqs.getEffectiveToughness(gd, unrelatedCreature)).isEqualTo(unrelatedBaseToughness);
    }

    @Test
    @DisplayName("The bonus updates when another Sliver leaves the battlefield")
    void bonusUpdatesWhenSliverLeaves() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new MetallicSliver());
        int legionBasePower = legion.getCard().getPower();
        int legionBaseToughness = legion.getCard().getToughness();
        int sliverBasePower = sliver.getCard().getPower();
        int sliverBaseToughness = sliver.getCard().getToughness();

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(sliverBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(sliverBaseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness);
    }
}
