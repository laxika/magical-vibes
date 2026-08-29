package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrilledCaveWurm.class, GrizzlyBears.class, Shock.class})
class FrilledCaveWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/5 with fewer than four permanent cards in its controller's graveyard")
    void noBoostBelowThreshold() {
        harness.setGraveyard(player1, permanentCards(3));
        harness.addToBattlefield(player1, new FrilledCaveWurm());

        assertStats(2, 5);
    }

    @Test
    @DisplayName("Gets +2/+0 with four permanent cards in its controller's graveyard")
    void boostAtThreshold() {
        harness.setGraveyard(player1, permanentCards(4));
        harness.addToBattlefield(player1, new FrilledCaveWurm());

        assertStats(4, 5);
    }

    @Test
    @DisplayName("Instant and sorcery cards do not count toward descend")
    void nonPermanentCardsDoNotCount() {
        List<Card> graveyard = permanentCards(3);
        graveyard.add(new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.addToBattlefield(player1, new FrilledCaveWurm());

        assertStats(2, 5);
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward descend")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, permanentCards(4));
        harness.addToBattlefield(player1, new FrilledCaveWurm());

        assertStats(2, 5);
    }

    @Test
    @DisplayName("Loses the bonus when its controller's graveyard drops below four permanent cards")
    void losesBoostWhenGraveyardShrinks() {
        harness.setGraveyard(player1, permanentCards(4));
        harness.addToBattlefield(player1, new FrilledCaveWurm());
        assertStats(4, 5);

        harness.setGraveyard(player1, permanentCards(3));
        assertStats(2, 5);
    }

    private List<Card> permanentCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void assertStats(int power, int toughness) {
        Permanent wurm = findPermanent(player1, "Frilled Cave-Wurm");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(toughness);
    }
}
