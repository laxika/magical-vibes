package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExpeditionSkulker.class, DeathcultRogue.class, GrizzlyBears.class})
class ExpeditionSkulkerTest extends BaseCardTest {

    @Test
    void gainsDeathtouchWhileControllingAnotherRogue() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player1, new DeathcultRogue());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void losesDeathtouchWhenNoLongerControllingAnotherRogue() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new DeathcultRogue());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(rogue);

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void nonRogueDoesNotEnableDeathtouch() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void opponentsRogueDoesNotEnableDeathtouch() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ExpeditionSkulker());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void twoSkulkersEnableEachOthersDeathtouch() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void retainsDeathtouchWhileAtLeastOneOtherRogueRemains() {
        Permanent skulker = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent firstSupport = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());
        Permanent secondSupport = harness.addToBattlefieldAndReturn(player1, new ExpeditionSkulker());

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstSupport);

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondSupport);

        assertThat(gqs.hasKeyword(gd, skulker, Keyword.DEATHTOUCH)).isFalse();
    }
}
