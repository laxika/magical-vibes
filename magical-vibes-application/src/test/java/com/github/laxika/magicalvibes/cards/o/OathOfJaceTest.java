package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfJace.class, Forest.class, GideonOfTheTrials.class, GrizzlyBears.class})
class OathOfJaceTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, draws three cards then makes its controller discard two cards")
    void entersDrawsThreeThenDiscardsTwo() {
        Card discardedOne = new GrizzlyBears();
        Card discardedTwo = new GrizzlyBears();
        harness.setHand(player1, List.of(new OathOfJace(), discardedOne, discardedTwo));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(discardedOne.getId(), discardedTwo.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(3)
                .allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("At upkeep, scries for the number of planeswalkers controlled")
    void upkeepScriesForPlaneswalkersControlled() {
        harness.addToBattlefield(player1, new OathOfJace());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonOfTheTrials());
        gideon.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("At upkeep, does not scry when no planeswalkers are controlled")
    void upkeepWithNoPlaneswalkersDoesNotScry() {
        harness.addToBattlefield(player1, new OathOfJace());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
