package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FleetfeatherSandalsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying and haste")
    void equippedCreatureHasFlyingAndHaste() {
        Permanent sandals = new Permanent(new FleetfeatherSandals());
        gd.playerBattlefields.get(player1.getId()).add(sandals);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        sandals.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keywords are lost when the Sandals are unattached")
    void keywordsLostWhenUnattached() {
        Permanent sandals = new Permanent(new FleetfeatherSandals());
        gd.playerBattlefields.get(player1.getId()).add(sandals);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        sandals.setAttachedTo(bears.getId());

        sandals.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains flying and haste")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent sandals = new Permanent(new FleetfeatherSandals());
        gd.playerBattlefields.get(player1.getId()).add(sandals);

        Permanent equipped = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(equipped);

        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(other);

        sandals.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }
}
