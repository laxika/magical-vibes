package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshnodsAltar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpringingTiger.class, AshnodsAltar.class})
class SpringingTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 3/3 with fewer than seven cards in its controller's graveyard")
    void noBoostBelowThreshold() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new SpringingTiger());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Gets +2/+2 with seven cards in its controller's graveyard")
    void getsBoostAtThreshold() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new SpringingTiger());

        assertStats(5, 5);
    }

    @Test
    @DisplayName("Only its controller's graveyard counts")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player1, new SpringingTiger());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Loses the boost when its controller's graveyard falls below seven cards")
    void losesBoostWhenGraveyardShrinks() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new SpringingTiger());
        assertStats(5, 5);

        harness.setGraveyard(player1, List.of(new AshnodsAltar(), new AshnodsAltar(), new AshnodsAltar(),
                new AshnodsAltar(), new AshnodsAltar(), new AshnodsAltar()));

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Gains the boost immediately when the graveyard reaches seven cards")
    void gainsBoostWhenGraveyardGrows() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new SpringingTiger());
        assertStats(3, 3);

        fillGraveyard(player1, 7);

        assertStats(5, 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost remains +2/+2 with more than seven cards")
    void boostDoesNotScaleWithGraveyardSize() {
        fillGraveyard(player1, 14);
        harness.addToBattlefield(player1, new SpringingTiger());

        assertStats(5, 5);
    }

    @Test
    @DisplayName("Each Tiger checks its own controller's graveyard")
    void opposingTigersUseSeparateGraveyards() {
        fillGraveyard(player1, 7);
        fillGraveyard(player2, 6);
        harness.addToBattlefield(player1, new SpringingTiger());
        harness.addToBattlefield(player2, new SpringingTiger());

        assertStats(5, 5);
        Permanent opposingTiger = findPermanent(player2, "Springing Tiger");
        assertThat(gqs.getEffectivePower(gd, opposingTiger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingTiger)).isEqualTo(3);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new AshnodsAltar());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(int power, int toughness) {
        Permanent tiger = findPermanent(player1, "Springing Tiger");
        assertThat(gqs.getEffectivePower(gd, tiger)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, tiger)).isEqualTo(toughness);
    }
}
