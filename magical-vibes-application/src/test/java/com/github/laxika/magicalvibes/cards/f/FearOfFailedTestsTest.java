package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FearOfFailedTests.class, Forest.class, Island.class})
class FearOfFailedTestsTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage draws that many cards")
    void combatDamageDrawsEqualToDamageDealt() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addCreatureReady(player1, new FearOfFailedTests());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
