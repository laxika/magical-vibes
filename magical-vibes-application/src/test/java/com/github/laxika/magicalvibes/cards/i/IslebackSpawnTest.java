package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IslebackSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Base 4/8 while both libraries have more than twenty cards")
    void noBoostWhenLibrariesLarge() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(8);
    }

    @Test
    @DisplayName("Gets +4/+8 when a library has exactly twenty cards")
    void boostAtExactlyTwenty() {
        harness.setLibrary(player1, filler(20));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Opponent's small library also grants the boost")
    void boostFromOpponentLibrary() {
        harness.setLibrary(player1, filler(21));
        harness.setLibrary(player2, filler(5));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(16);
    }

    @Test
    @DisplayName("Loses the boost once every library exceeds twenty cards again")
    void losesBoostWhenLibrariesGrow() {
        harness.setLibrary(player1, filler(10));
        harness.setLibrary(player2, filler(21));
        harness.addToBattlefield(player1, new IslebackSpawn());

        Permanent spawn = findSpawn();
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(8);

        harness.setLibrary(player1, filler(21));
        assertThat(gqs.getEffectivePower(gd, spawn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(8);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private Permanent findSpawn() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Isleback Spawn"))
                .findFirst().orElseThrow();
    }
}
