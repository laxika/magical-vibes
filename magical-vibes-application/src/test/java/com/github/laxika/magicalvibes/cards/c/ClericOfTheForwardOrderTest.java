package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ClericOfTheForwardOrder.class, Disperse.class})
class ClericOfTheForwardOrderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 2 life counting itself")
    void gainsTwoLifeAlone() {
        cast(player1);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB gains 2 life for each copy you control")
    void gainsTwoLifePerCopy() {
        addCreatureReady(player1, new ClericOfTheForwardOrder());
        addCreatureReady(player1, new ClericOfTheForwardOrder());

        cast(player1);

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Copies controlled by the opponent are not counted")
    void ignoresOpponentCopies() {
        addCreatureReady(player2, new ClericOfTheForwardOrder());

        cast(player1);

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing the only Cleric before its trigger resolves gains no life")
    void gainsNoLifeWhenSourceLeavesBeforeResolution() {
        castLeavingTriggerOnStack();
        bounce(harness.getPermanentId(player1, "Cleric of the Forward Order"));

        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Cleric of the Forward Order");
        harness.assertNotOnBattlefield(player1, "Cleric of the Forward Order");
    }

    @Test
    @DisplayName("A removed source still gains life for another Cleric")
    void triggerResolvesAfterSourceLeaves() {
        castLeavingTriggerOnStack();
        var sourceId = harness.getPermanentId(player1, "Cleric of the Forward Order");
        addCreatureReady(player1, new ClericOfTheForwardOrder());
        bounce(sourceId);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Copies removed in response are not counted at resolution")
    void countsCopiesAtResolution() {
        var other = addCreatureReady(player1, new ClericOfTheForwardOrder());
        castLeavingTriggerOnStack();
        bounce(other.getId());

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    private void castLeavingTriggerOnStack() {
        harness.castFromHand(player1, new ClericOfTheForwardOrder(), "{1}{W}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
    }

    private void bounce(java.util.UUID targetId) {
        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }

    private void cast(Player player) {
        harness.castFromHand(player, new ClericOfTheForwardOrder(), "{1}{W}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }
}
