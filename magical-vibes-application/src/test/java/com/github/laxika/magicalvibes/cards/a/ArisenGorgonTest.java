package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LilianaTheNecromancer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArisenGorgonTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have deathtouch without a Liliana planeswalker")
    void noDeathtouchWithoutLiliana() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new ArisenGorgon());

        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Has deathtouch while its controller controls a Liliana planeswalker")
    void hasDeathtouchWithLiliana() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new ArisenGorgon());
        harness.addToBattlefield(player1, new LilianaTheNecromancer());

        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Liliana planeswalker does not grant deathtouch")
    void opponentLilianaDoesNotCount() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new ArisenGorgon());
        harness.addToBattlefield(player2, new LilianaTheNecromancer());

        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Loses deathtouch when the Liliana planeswalker leaves")
    void losesDeathtouchWhenLilianaLeaves() {
        Permanent gorgon = harness.addToBattlefieldAndReturn(player1, new ArisenGorgon());
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaTheNecromancer());

        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(liliana);

        assertThat(gqs.hasKeyword(gd, gorgon, Keyword.DEATHTOUCH)).isFalse();
    }
}
