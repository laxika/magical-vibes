package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NyleasForerunner.class, NyxbornColossus.class})
class NyleasForerunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have trample")
    void ownCreaturesGainTrample() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        harness.addToBattlefield(player1, new NyleasForerunner());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain trample")
    void opponentCreaturesDoNotGainTrample() {
        Permanent opponentCreature = addCreatureReady(player2, new NyxbornColossus());
        harness.addToBattlefield(player1, new NyleasForerunner());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is removed when Nylea's Forerunner leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent forerunner = addCreatureReady(player1, new NyleasForerunner());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(forerunner);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the Forerunner also gain trample")
    void laterCreaturesGainTrample() {
        harness.addToBattlefield(player1, new NyleasForerunner());

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new NyxbornColossus());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains until the last Forerunner leaves")
    void multipleSourcesMaintainTrample() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());
        Permanent first = addCreatureReady(player1, new NyleasForerunner());
        Permanent second = addCreatureReady(player1, new NyleasForerunner());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(second);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting the Forerunner grants trample only after it resolves")
    void abilityStartsWhenCreatureSpellResolves() {
        Permanent creature = addCreatureReady(player1, new NyxbornColossus());

        harness.castFromHand(player1, new NyleasForerunner(), "{4}{G}");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nylea's Forerunner");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }
}
