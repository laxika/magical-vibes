package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GurglingAnointerTest extends BaseCardTest {

    @Test
    @DisplayName("Putting the second card drawn each turn adds a +1/+1 counter")
    void secondDrawAddsCounter() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(anointer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(anointer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, it returns another creature card within its last-known power")
    void deathReturnsAnotherCreatureWithinPower() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        anointer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, anointer.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(eligible.getId());
        assertThat(choice.validCardIds()).doesNotContain(tooExpensive.getId(), anointer.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Gurgling Anointer");
    }

    @Test
    @DisplayName("Its death trigger does not resolve without an eligible creature card")
    void deathTriggerNeedsEligibleCreature() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        harness.setGraveyard(player1, List.of(new HillGiant()));

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, anointer.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Gurgling Anointer");
        harness.assertInGraveyard(player1, "Hill Giant");
    }
}
