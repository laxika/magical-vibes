package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SoulOfTheRapidsTest extends BaseCardTest {

    @Test
    @DisplayName("Soul of the Rapids has flying and hexproof")
    void hasFlyingAndHexproof() {
        Permanent soul = new Permanent(new SoulOfTheRapids());
        gd.playerBattlefields.get(player1.getId()).add(soul);

        assertThat(gqs.hasKeyword(gd, soul, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, soul, Keyword.HEXPROOF)).isTrue();
    }
}
