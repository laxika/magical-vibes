package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KhenraCharioteer.class, GrizzlyBears.class, PouncingCheetah.class})
class KhenraCharioteerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void ownCreaturesGainTrample() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new KhenraCharioteer());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain trample")
    void opponentCreaturesDoNotGainTrample() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new KhenraCharioteer());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is removed when Khenra Charioteer leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new KhenraCharioteer());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Khenra Charioteer"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering later immediately gain trample")
    void laterCreaturesGainTrample() {
        harness.addToBattlefield(player1, new KhenraCharioteer());

        Permanent cheetah = harness.enterBattlefieldAndReturn(player1, new PouncingCheetah());
        Permanent opponentCheetah = harness.enterBattlefieldAndReturn(player2, new PouncingCheetah());

        assertThat(gqs.hasKeyword(gd, cheetah, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCheetah, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Another Charioteer continues granting trample after one leaves")
    void trampleRemainsWhileAnotherSourceIsPresent() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KhenraCharioteer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KhenraCharioteer());
        Permanent cheetah = harness.enterBattlefieldAndReturn(player1, new PouncingCheetah());
        assertThat(gqs.hasKeyword(gd, cheetah, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, cheetah, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, cheetah, Keyword.TRAMPLE)).isFalse();
    }
}
