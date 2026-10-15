package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MadrushCyclops.class, GrizzledLeotau.class})
class MadrushCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain haste")
    void ownCreaturesGainHaste() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        harness.addToBattlefield(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Madrush Cyclops himself gains haste")
    void madrushGainsHaste() {
        Permanent madrush = harness.addToBattlefieldAndReturn(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, madrush, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain haste")
    void opponentCreaturesDoNotGainHaste() {
        Permanent opponentLeotau = harness.addToBattlefieldAndReturn(player2, new GrizzledLeotau());
        harness.addToBattlefield(player1, new MadrushCyclops());

        assertThat(gqs.hasKeyword(gd, opponentLeotau, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is removed when Madrush Cyclops leaves the battlefield")
    void hasteRemovedWhenSourceLeaves() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        harness.addToBattlefield(player1, new MadrushCyclops());
        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Madrush Cyclops"));

        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after Madrush Cyclops immediately have haste")
    void laterCreaturesGainHaste() {
        harness.addToBattlefield(player1, new MadrushCyclops());

        Permanent leotau = harness.enterBattlefieldAndReturn(player1, new GrizzledLeotau());

        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste remains while another Madrush Cyclops is on the battlefield")
    void hasteRemainsUntilLastSourceLeaves() {
        Permanent leotau = harness.addToBattlefieldAndReturn(player1, new GrizzledLeotau());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MadrushCyclops());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MadrushCyclops());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, leotau, Keyword.HASTE)).isFalse();
    }
}
