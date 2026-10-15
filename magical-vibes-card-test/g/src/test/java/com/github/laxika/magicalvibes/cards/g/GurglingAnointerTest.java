package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GurglingAnointer.class, GrizzlyBears.class, HillGiant.class, Murder.class, GoForTheThroat.class,
        Memnite.class})
class GurglingAnointerTest extends BaseCardTest {

    @Test
    @DisplayName("Putting the second card drawn each turn adds a +1/+1 counter")
    void secondDrawAddsCounter() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

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
        harness.castAndResolveInstant(player1, 0, anointer.getId());

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
        harness.castAndResolveInstant(player1, 0, anointer.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Gurgling Anointer");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Drawing a third card does not add another counter")
    void thirdDrawDoesNotAddCounter() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        harness.setLibrary(player1, List.of(new GurglingAnointer(), new GurglingAnointer(),
                new GurglingAnointer()));

        for (int i = 0; i < 3; i++) {
            harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
            harness.passBothPriorities();
        }

        assertThat(anointer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's second draw does not add a counter")
    void opponentsSecondDrawDoesNotAddCounter() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        harness.setLibrary(player2, List.of(new GurglingAnointer(), new GurglingAnointer()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(anointer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another Anointer at exactly the dying Anointer's power is eligible")
    void returnsAnotherCopyAtExactPower() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        anointer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card otherAnointer = new GurglingAnointer();
        harness.setGraveyard(player1, List.of(otherAnointer));
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, anointer.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(otherAnointer.getId());

        harness.handleMultipleCardsChosen(player1, List.of(otherAnointer.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(otherAnointer.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(anointer.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(otherAnointer);
    }

    @Test
    @DisplayName("Negative last-known power cannot target a zero-mana creature")
    void negativePowerCannotReturnZeroManaCreature() {
        Permanent anointer = harness.addToBattlefieldAndReturn(player1, new GurglingAnointer());
        anointer.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, anointer.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Memnite");
        harness.assertNotOnBattlefield(player1, "Memnite");
        harness.assertInGraveyard(player1, "Gurgling Anointer");
    }
}
