package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimanaSkitterSneak.class, Spellbook.class})
class NimanaSkitterSneakTest extends BaseCardTest {

    @Test
    @DisplayName("Has base stats without enough cards in an opponent's graveyard")
    void baseStatsBelowThreshold() {
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and menace when an opponent has eight cards in their graveyard")
    void boostAtThreshold() {
        fillGraveyard(player2, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The controller's graveyard does not satisfy the condition")
    void ownGraveyardDoesNotCount() {
        fillGraveyard(player1, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, findSneak(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses the bonus when the opponent's graveyard drops below eight cards")
    void losesBonusWhenGraveyardShrinks() {
        fillGraveyard(player2, 8);
        harness.addToBattlefield(player1, new NimanaSkitterSneak());
        Permanent sneak = findSneak();

        assertStats(4, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isTrue();

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertStats(3, 4);
        assertThat(gqs.hasKeyword(gd, sneak, Keyword.MENACE)).isFalse();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private Permanent findSneak() {
        return findPermanent(player1, "Nimana Skitter-Sneak");
    }

    private void assertStats(int power, int toughness) {
        Permanent sneak = findSneak();
        assertThat(gqs.getEffectivePower(gd, sneak)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, sneak)).isEqualTo(toughness);
    }
}
