package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BogwaterLumaret.class, GrizzlyBears.class})
class BogwaterLumaretTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield triggers self life gain")
    void selfEntryTriggersLifeGain() {
        harness.castFromHand(player1, new BogwaterLumaret(), "{B}{G}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Resolving self-ETB trigger gains 1 life")
    void selfEntryGainsOneLife() {
        harness.castFromHand(player1, new BogwaterLumaret(), "{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Another creature entering triggers life gain")
    void anotherCreatureEnteringTriggersLifeGain() {
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Another creature entering resolves and gains 1 life")
    void anotherCreatureGainsOneLife() {
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        harness.addToBattlefield(player2, new BogwaterLumaret());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Bogwater Lumarets trigger separately for a creature entering")
    void twoLumaretsTriggerSeparately() {
        harness.addToBattlefield(player1, new BogwaterLumaret());
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A second Lumaret gains life for both its own entry and the existing Lumaret")
    void enteringLumaretTriggersItselfAndExistingLumaret() {
        harness.addToBattlefield(player1, new BogwaterLumaret());
        harness.castFromHand(player1, new BogwaterLumaret(), "{B}{G}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
