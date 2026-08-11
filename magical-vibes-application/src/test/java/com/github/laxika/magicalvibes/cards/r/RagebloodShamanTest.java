package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MinotaurSkullcleaver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RagebloodShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Other Minotaur creatures you control get +1/+1 and have trample")
    void boostsOtherMinotaursAndGrantsTrample() {
        addCreatureReady(player1, new RagebloodShaman());
        Permanent minotaur = addCreatureReady(player1, new MinotaurSkullcleaver());

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Rageblood Shaman does not boost itself")
    void doesNotBoostItself() {
        Permanent shaman = addCreatureReady(player1, new RagebloodShaman());

        assertThat(gqs.getEffectivePower(gd, shaman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, shaman)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Minotaur creatures you control are unaffected")
    void doesNotBoostNonMinotaurs() {
        addCreatureReady(player1, new RagebloodShaman());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent Minotaurs are unaffected")
    void doesNotBoostOpponentMinotaurs() {
        Permanent minotaur = addCreatureReady(player2, new MinotaurSkullcleaver());
        addCreatureReady(player1, new RagebloodShaman());

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The bonus is removed when Rageblood Shaman leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        addCreatureReady(player1, new RagebloodShaman());
        Permanent minotaur = addCreatureReady(player1, new MinotaurSkullcleaver());
        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Rageblood Shaman"));

        assertThat(gqs.getEffectivePower(gd, minotaur)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, minotaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.TRAMPLE)).isFalse();
    }
}
