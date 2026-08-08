package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrostanisSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a Knight, a Centaur and a Rhino token")
    void etbCreatesAllThreeTokens() {
        castSummoner();
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger

        assertToken(player1, "Knight", 2, 2, Keyword.VIGILANCE);
        assertToken(player1, "Centaur", 3, 3, null);
        assertToken(player1, "Rhino", 4, 4, Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Tokens are created under the controller's control only")
    void opponentGetsNoTokens() {
        castSummoner();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokens(player2, "Knight")).isEmpty();
        assertThat(tokens(player2, "Centaur")).isEmpty();
        assertThat(tokens(player2, "Rhino")).isEmpty();
    }

    private void castSummoner() {
        harness.setHand(player1, List.of(new TrostanisSummoner()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
    }

    private void assertToken(Player player, String name, int power, int toughness, Keyword keyword) {
        List<Permanent> found = tokens(player, name);
        assertThat(found).hasSize(1);
        Permanent token = found.getFirst();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(toughness);
        if (keyword != null) {
            assertThat(token.getCard().getKeywords()).contains(keyword);
        }
    }

    private List<Permanent> tokens(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .toList();
    }
}
