package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlewiseAven.class, GrizzlyBears.class})
class BattlewiseAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Has no threshold bonus with fewer than seven cards in its controller's graveyard")
    void noThresholdBonus() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new BattlewiseAven());

        assertStats(2, 2);
        assertThat(gqs.hasKeyword(gd, findAven(), Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+1 and first strike with seven cards in its controller's graveyard")
    void thresholdBonus() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new BattlewiseAven());

        assertStats(3, 3);
        assertThat(gqs.hasKeyword(gd, findAven(), Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardWithCards(7));
        harness.addToBattlefield(player1, new BattlewiseAven());

        assertStats(2, 2);
        assertThat(gqs.hasKeyword(gd, findAven(), Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Loses the threshold bonus when its controller's graveyard drops below seven cards")
    void losesThresholdBonusWhenGraveyardChanges() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.addToBattlefield(player1, new BattlewiseAven());
        Permanent aven = findAven();

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aven, Keyword.FIRST_STRIKE)).isTrue();

        harness.setGraveyard(player1, List.of());

        assertStats(2, 2);
        assertThat(gqs.hasKeyword(gd, aven, Keyword.FIRST_STRIKE)).isFalse();
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }

    private Permanent findAven() {
        return findPermanent(player1, "Battlewise Aven");
    }

    private void assertStats(int power, int toughness) {
        Permanent aven = findAven();
        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(toughness);
    }
}
