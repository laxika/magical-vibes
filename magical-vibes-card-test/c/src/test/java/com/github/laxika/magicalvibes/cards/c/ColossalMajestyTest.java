package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BristlingBoar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColossalMajesty.class, ColossalDreadmaw.class, CentaurCourser.class, BristlingBoar.class})
class ColossalMajestyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you control a creature with power 4 or greater")
    void drawsWhenControllingCreatureWithPowerAtLeastFour() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw when your creatures all have power less than 4")
    void doesNotDrawBelowPowerThreshold() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new CentaurCourser());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not draw when only an opponent controls a creature with power 4 or greater")
    void doesNotDrawFromOpponentsCreature() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void drawsForExactlyFourPower() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new BristlingBoar());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void countsPowerFromCounters() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        var creature = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void doesNotDrawWhenPowerFallsBelowFourBeforeResolution() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        var creature = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        creature.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerIfCreatureOnlyReachesFourPowerAfterUpkeepBegins() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        var creature = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void drawsWhenADifferentCreatureMeetsTheConditionAtResolution() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        var originalCreature = harness.addToBattlefieldAndReturn(player1, new BristlingBoar());
        var otherCreature = harness.addToBattlefieldAndReturn(player1, new CentaurCourser());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        originalCreature.setPowerModifier(-1);
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void drawsOnlyOneCardForMultipleQualifyingCreatures() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new BristlingBoar());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
