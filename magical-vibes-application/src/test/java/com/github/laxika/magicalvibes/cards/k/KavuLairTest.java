package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuLair.class, KavuChameleon.class, KavuTitan.class})
class KavuLairTest extends BaseCardTest {

    @Test
    @DisplayName("The entering creature's controller draws for a creature with power 4 or greater")
    void enteringCreatureControllerDrawsForBigCreature() {
        harness.addToBattlefield(player1, new KavuLair());
        harness.setHand(player1, List.of(new KavuChameleon()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's entering creature makes that opponent draw")
    void opponentDrawsForTheirBigCreature() {
        harness.addToBattlefield(player1, new KavuLair());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KavuChameleon()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for a creature with power less than 4")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new KavuLair());
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Triggers when a creature enters with power 4 or greater from counters")
    void triggersForPowerAfterEnteringCounters() {
        harness.addToBattlefield(player1, new KavuLair());
        harness.setHand(player1, List.of(new KavuTitan()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
