package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrenchStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have deathtouch or lifelink before its controller draws two cards")
    void lacksKeywordsBeforeThreshold() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Gains deathtouch and lifelink after its controller draws two cards")
    void gainsKeywordsAtThreshold() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
            harness.getDrawService().resolveDrawCard(gd, player1.getId());
        });

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not enable the ability")
    void opponentDrawsDoNotCount() {
        Permanent stalker = addCreatureReady(player1, new TrenchStalker());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
            harness.getDrawService().resolveDrawCard(gd, player2.getId());
        });

        assertThat(gqs.hasKeyword(gd, stalker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, stalker, Keyword.LIFELINK)).isFalse();
    }
}
