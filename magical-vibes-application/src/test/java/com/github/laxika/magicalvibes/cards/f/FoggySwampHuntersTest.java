package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoggySwampHunters.class, GrizzlyBears.class})
class FoggySwampHuntersTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have lifelink or menace before its controller draws two cards")
    void noKeywordsBeforeTwoDraws() {
        Permanent hunters = addHunters();

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has lifelink and menace after its controller draws two cards")
    void gainsKeywordsAfterTwoDraws() {
        Permanent hunters = addHunters();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();

        draw(player1);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's draws do not grant the keywords")
    void opponentDrawsDoNotCount() {
        Permanent hunters = addHunters();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);

        assertThat(gqs.hasKeyword(gd, hunters, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, hunters, Keyword.MENACE)).isFalse();
    }

    private Permanent addHunters() {
        return harness.addToBattlefieldAndReturn(player1, new FoggySwampHunters());
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
