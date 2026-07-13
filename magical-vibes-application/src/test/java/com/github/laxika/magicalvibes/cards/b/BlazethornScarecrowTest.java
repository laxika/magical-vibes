package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlazethornScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Has neither haste nor wither with no red or green creature")
    void noKeywordsByDefault() {
        harness.addToBattlefield(player1, new BlazethornScarecrow());
        Permanent scarecrow = find(player1, "Blazethorn Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Has haste while you control a red creature")
    void hasHasteWithRedCreature() {
        harness.addToBattlefield(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new HillGiant()); // red
        Permanent scarecrow = find(player1, "Blazethorn Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Has wither while you control a green creature")
    void hasWitherWithGreenCreature() {
        harness.addToBattlefield(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new GrizzlyBears()); // green
        Permanent scarecrow = find(player1, "Blazethorn Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("An opponent's red creature does not grant haste")
    void opponentRedCreatureDoesNotGrantHaste() {
        harness.addToBattlefield(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player2, new HillGiant()); // red, opponent
        Permanent scarecrow = find(player1, "Blazethorn Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses haste when the red creature leaves the battlefield")
    void losesHasteWhenRedCreatureLeaves() {
        harness.addToBattlefield(player1, new BlazethornScarecrow());
        harness.addToBattlefield(player1, new HillGiant());
        Permanent scarecrow = find(player1, "Blazethorn Scarecrow");

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isTrue();

        Permanent red = find(player1, "Hill Giant");
        gd.playerBattlefields.get(player1.getId()).remove(red);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.HASTE)).isFalse();
    }

    private Permanent find(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
