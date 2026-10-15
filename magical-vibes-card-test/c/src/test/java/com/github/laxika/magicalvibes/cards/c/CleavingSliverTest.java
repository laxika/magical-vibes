package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CleavingSliver.class, BonescytheSliver.class, GrizzlyBears.class})
class CleavingSliverTest extends BaseCardTest {

    @Test
    void boostsSelf() {
        Permanent sliver = addCreatureReady(player1, new CleavingSliver());

        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(2);
    }

    @Test
    void boostsOtherSliverYouControl() {
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, otherSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, otherSliver);

        addCreatureReady(player1, new CleavingSliver());

        assertThat(gqs.getEffectivePower(gd, otherSliver)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, otherSliver)).isEqualTo(baseToughness);
    }

    @Test
    void doesNotBoostNonSliverCreatures() {
        addCreatureReady(player1, new CleavingSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void doesNotBoostOpponentSlivers() {
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());
        int basePower = gqs.getEffectivePower(gd, opponentSliver);
        int baseToughness = gqs.getEffectiveToughness(gd, opponentSliver);

        addCreatureReady(player1, new CleavingSliver());

        assertThat(gqs.getEffectivePower(gd, opponentSliver)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponentSliver)).isEqualTo(baseToughness);
    }

    @Test
    void multipleCopiesStackTheirBonuses() {
        Permanent first = addCreatureReady(player1, new CleavingSliver());
        Permanent second = addCreatureReady(player1, new CleavingSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void bonusEndsWhenItsSourceDies() {
        Permanent source = addCreatureReady(player1, new CleavingSliver());
        Permanent survivor = addCreatureReady(player1, new CleavingSliver());
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(6);

        source.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source).contains(survivor);
        harness.assertInGraveyard(player1, "Cleaving Sliver");
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(2);
    }

    @Test
    void bonusBeginsOnlyAfterSpellResolves() {
        Permanent existing = addCreatureReady(player1, new CleavingSliver());

        harness.castFromHand(player1, new CleavingSliver(), "{3}{R}");

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(4);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cleaving Sliver")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, existing)).isEqualTo(2);
    }
}
