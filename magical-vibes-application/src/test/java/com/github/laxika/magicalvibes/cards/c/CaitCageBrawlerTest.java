package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaitCageBrawler.class, GrizzlyBears.class, Forest.class})
class CaitCageBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws for both players and puts counters on Cait when the discards tie")
    void tiedDiscardManaValuesPutCountersOnCait() {
        Permanent cait = attackWith(
                List.of(new GrizzlyBears()), new Forest(),
                List.of(new GrizzlyBears()), new Forest());

        discardByName(player1, "Grizzly Bears");
        discardByName(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, cait)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cait)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cait gets no counters when the defending player discards a higher-mana-value card")
    void higherDefendingDiscardDoesNotPutCountersOnCait() {
        Permanent cait = attackWith(
                List.of(new Forest()), new Forest(),
                List.of(new GrizzlyBears()), new Forest());

        discardByName(player1, "Forest");
        discardByName(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, cait)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cait)).isEqualTo(1);
    }

    private Permanent attackWith(List<Card> controllerHand, Card controllerDraw,
            List<Card> defendingHand, Card defendingDraw) {
        Permanent cait = addCreatureReady(player1, new CaitCageBrawler());
        harness.setHand(player1, controllerHand);
        harness.setHand(player2, defendingHand);
        harness.setLibrary(player1, List.of(controllerDraw));
        harness.setLibrary(player2, List.of(defendingDraw));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        return cait;
    }

    private void discardByName(Player player, String cardName) {
        List<Card> hand = gd.playerHands.get(player.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' present in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player, index);
    }
}
