package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderclapWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts another flier you control")
    void boostsOtherOwnFlier() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent hawk = addCreatureReady(player1, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost itself")
    void doesNotBoostItself() {
        Permanent wyvern = addCreatureReady(player1, new ThunderclapWyvern());

        assertThat(gqs.getEffectivePower(gd, wyvern)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wyvern)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost a ground creature you control")
    void doesNotBoostGroundCreature() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost an opponent's flier")
    void doesNotBoostOpponentFlier() {
        addCreatureReady(player1, new ThunderclapWyvern());
        Permanent opponentHawk = addCreatureReady(player2, new SuntailHawk());

        assertThat(gqs.getEffectivePower(gd, opponentHawk)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentHawk)).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Wyverns boost each other")
    void twoWyvernsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new ThunderclapWyvern());
        Permanent second = addCreatureReady(player1, new ThunderclapWyvern());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }
}
