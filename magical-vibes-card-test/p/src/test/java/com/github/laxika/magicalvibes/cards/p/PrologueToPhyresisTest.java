package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrologueToPhyresis.class})
class PrologueToPhyresisTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent gets a poison counter and the controller draws a card")
    void poisonsEachOpponentAndDrawsCard() {
        harness.setLibrary(player1, List.of(new PrologueToPhyresis()));
        harness.castFromHand(player1, new PrologueToPhyresis(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertInHand(player1, "Prologue to Phyresis");
    }

    @Test
    @DisplayName("Poison is added to existing counters relative to the spell's controller")
    void addsPoisonRelativeToSpellController() {
        gd.playerPoisonCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setLibrary(player2, List.of(new PrologueToPhyresis(), new PrologueToPhyresis()));

        harness.castFromHand(player2, new PrologueToPhyresis(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Prologue to Phyresis");
        harness.assertInGraveyard(player2, "Prologue to Phyresis");
    }
}
