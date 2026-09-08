package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GlazeFiend;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeethingWurmletTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and gets a counter on the first artifact trigger to resolve")
    void firstArtifactTriggerGainsLifeAndAddsCounter() {
        Permanent wurmlet = harness.addToBattlefieldAndReturn(player1, new TeethingWurmlet());
        castArtifactAndResolve(new GlazeFiend());

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(wurmlet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Later artifact triggers still gain life but do not add another first-resolution counter")
    void laterArtifactTriggersOnlyGainLife() {
        Permanent wurmlet = harness.addToBattlefieldAndReturn(player1, new TeethingWurmlet());
        castArtifactAndResolve(new GlazeFiend());
        castArtifactAndResolve(new GlazeFiend());

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(wurmlet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has deathtouch exactly while its controller has at least three artifacts")
    void deathtouchUsesMetalcraft() {
        Permanent wurmlet = harness.addToBattlefieldAndReturn(player1, new TeethingWurmlet());
        harness.addToBattlefield(player1, new GlazeFiend());
        harness.addToBattlefield(player1, new GlazeFiend());

        assertThat(gqs.hasKeyword(gd, wurmlet, Keyword.DEATHTOUCH)).isFalse();

        Permanent thirdArtifact = harness.addToBattlefieldAndReturn(player1, new GlazeFiend());
        assertThat(gqs.hasKeyword(gd, wurmlet, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(thirdArtifact);
        assertThat(gqs.hasKeyword(gd, wurmlet, Keyword.DEATHTOUCH)).isFalse();
    }

    private void castArtifactAndResolve(GlazeFiend artifact) {
        harness.setHand(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();
    }
}
