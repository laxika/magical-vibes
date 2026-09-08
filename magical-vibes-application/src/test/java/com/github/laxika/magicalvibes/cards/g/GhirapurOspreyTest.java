package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhirapurOspreyTest extends BaseCardTest {

    @Test
    @DisplayName("Ghirapur Osprey has flying")
    void hasFlying() {
        Permanent osprey = new Permanent(new GhirapurOsprey());
        gd.playerBattlefields.get(player1.getId()).add(osprey);

        assertThat(gqs.hasKeyword(gd, osprey, Keyword.FLYING)).isTrue();
    }
}
