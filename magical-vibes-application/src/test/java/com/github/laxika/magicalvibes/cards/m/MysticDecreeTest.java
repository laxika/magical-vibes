package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticDecree.class, AirElemental.class, BullHippo.class})
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
        Permanent hippo = harness.addToBattlefieldAndReturn(player2, new BullHippo());

        assertThat(gqs.hasKeyword(gd, hippo, Keyword.ISLANDWALK)).isTrue();

        harness.addToBattlefieldAndReturn(player1, new MysticDecree());

        assertThat(gqs.hasKeyword(gd, hippo, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the decree also lose both keywords")
    void stripsKeywordsFromCreaturesEnteringLater() {
        harness.addToBattlefieldAndReturn(player1, new MysticDecree());

        Permanent flier = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent islandwalker = harness.addToBattlefieldAndReturn(player2, new BullHippo());

        assertThat(gqs.hasKeyword(gd, flier, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, islandwalker, Keyword.ISLANDWALK)).isFalse();
    }
}
