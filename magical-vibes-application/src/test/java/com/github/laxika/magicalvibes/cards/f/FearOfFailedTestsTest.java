package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

    @Test
    void combatDamageToCreatureDoesNotDrawCards() {
        Card draw = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(draw));
        addCreatureReady(player1, new FearOfFailedTests());
        addCreatureReady(player2, new FearOfFailedTests());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertLife(player2, 20);
    }

    @Test
    void zeroPowerDoesNotTriggerDraw() {
        Card draw = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(draw));
        addCreatureReady(player1, new FearOfFailedTests()).setPowerModifier(-2);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
        harness.assertLife(player2, 20);
    }

    @Test
    void otherControllerDrawsForModifiedCombatDamage() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        Card thirdDraw = new Forest();
        Card remaining = new Island();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, thirdDraw, remaining));
        addCreatureReady(player2, new FearOfFailedTests()).setPowerModifier(1);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 17);
    }
}
