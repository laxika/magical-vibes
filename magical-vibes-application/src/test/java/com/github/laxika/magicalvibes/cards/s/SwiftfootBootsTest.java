package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftfootBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has hexproof and haste")
    void equippedCreatureHasHexproofAndHaste() {
        Permanent boots = new Permanent(new SwiftfootBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();

        boots.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Keywords are lost when the Boots are unattached")
    void keywordsLostWhenUnattached() {
        Permanent boots = new Permanent(new SwiftfootBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        boots.setAttachedTo(bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        boots.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains the keywords")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent boots = new Permanent(new SwiftfootBoots());
        gd.playerBattlefields.get(player1.getId()).add(boots);

        Permanent equipped = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(equipped);

        Permanent other = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(other);

        boots.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }
}
