package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudshredderSliver.class, GrizzlyBears.class, BonescytheSliver.class})
class CloudshredderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets newly entered Slivers attack until the source leaves")
    void newlyEnteredSliversCanAttackWhileSourceRemains() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new CloudshredderSliver());
        Permanent other = harness.enterBattlefieldAndReturn(player1, new BonescytheSliver());
        source.setSummoningSick(true);
        other.setSummoningSick(true);

        assertThat(als.canAttack(gd, source, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, other, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(als.canAttack(gd, other, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Another Cloudshredder keeps granting keywords when one source leaves")
    void keywordsRemainUntilLastSourceLeaves() {
        Permanent first = addCreatureReady(player1, new CloudshredderSliver());
        Permanent second = addCreatureReady(player1, new CloudshredderSliver());
        Permanent other = addCreatureReady(player1, new BonescytheSliver());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The grant follows the source's current controller")
    void grantFollowsSourceController() {
        Permanent source = addCreatureReady(player1, new CloudshredderSliver());
        Permanent formerControllersSliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent newControllersSliver = addCreatureReady(player2, new BonescytheSliver());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        assertThat(gqs.hasKeyword(gd, formerControllersSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, formerControllersSliver, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, newControllersSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, newControllersSliver, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cloudshredder Sliver grants itself flying and haste")
    void grantsSelfFlyingAndHaste() {
        Permanent sliver = addCreatureReady(player1, new CloudshredderSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants flying and haste to another Sliver you control")
    void grantsKeywordsToOtherSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant keywords to non-Slivers or opposing Slivers")
    void onlyGrantsToSliversYouControl() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.HASTE)).isFalse();
    }
}

@CardUsed({CloudshredderSliver.class, GrizzlyBears.class, BonescytheSliver.class})
class Mh1CloudshredderSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Cloudshredder Sliver grants itself flying and haste")
    void grantsKeywordsToItself() {
        Permanent cloudshredderSliver = addCreatureReady(player1, new CloudshredderSliver());

        assertThat(gqs.hasKeyword(gd, cloudshredderSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, cloudshredderSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants flying and haste to another Sliver you control")
    void grantsKeywordsToAnotherSliverYouControl() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant keywords to an opponent's Sliver")
    void doesNotGrantKeywordsToOpposingSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant keywords to a non-Sliver creature")
    void doesNotGrantKeywordsToNonSliver() {
        addCreatureReady(player1, new CloudshredderSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords are removed when Cloudshredder Sliver leaves")
    void losesKeywordsWhenSourceLeaves() {
        Permanent cloudshredderSliver = addCreatureReady(player1, new CloudshredderSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloudshredderSliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.HASTE)).isFalse();
    }
}
