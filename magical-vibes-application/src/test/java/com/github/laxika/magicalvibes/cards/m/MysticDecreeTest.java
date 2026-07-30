package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HarborSerpent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MysticDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures on both sides lose flying while the decree is on the battlefield")
    void stripsFlyingFromAllCreatures() {
        Permanent ownFlier = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentFlier = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, ownFlier, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentFlier, Keyword.FLYING)).isTrue();

        Permanent decree = harness.addToBattlefieldAndReturn(player1, new MysticDecree());

        assertThat(gqs.hasKeyword(gd, ownFlier, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentFlier, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(decree);

        assertThat(gqs.hasKeyword(gd, ownFlier, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentFlier, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Creatures lose islandwalk while the decree is on the battlefield")
    void stripsIslandwalkFromAllCreatures() {
        Permanent serpent = harness.addToBattlefieldAndReturn(player2, new HarborSerpent());

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.ISLANDWALK)).isTrue();

        harness.addToBattlefieldAndReturn(player1, new MysticDecree());

        assertThat(gqs.hasKeyword(gd, serpent, Keyword.ISLANDWALK)).isFalse();
    }
}
