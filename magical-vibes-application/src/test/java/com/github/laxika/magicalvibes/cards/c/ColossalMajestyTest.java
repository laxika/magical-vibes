package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player1, new HillGiant());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not draw when only an opponent controls a creature with power 4 or greater")
    void doesNotDrawFromOpponentsCreature() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new ColossalMajesty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }
}
