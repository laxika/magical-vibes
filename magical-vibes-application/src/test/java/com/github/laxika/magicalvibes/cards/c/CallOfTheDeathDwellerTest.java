package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallOfTheDeathDweller.class, GrizzlyBears.class, LlanowarElves.class, HillGiant.class})
class CallOfTheDeathDwellerTest extends BaseCardTest {

    @Test
    void returnsTwoCreaturesAndLetsYouChooseEachCounterTarget() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.maxTotalManaValue()).isEqualTo(3);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        Permanent returnedElves = findPermanent(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, returnedBears.getId());
        assertThat(returnedBears.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, returnedElves.getId());
        harness.passBothPriorities();

        assertThat(returnedBears.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(returnedElves.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(returnedElves.getCounterCount(CounterType.MENACE)).isEqualTo(1);
    }

    @Test
    void enforcesTheTargetCountAndCombinedManaValueLimit() {
        Card firstBears = new GrizzlyBears();
        Card secondBears = new GrizzlyBears();
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(firstBears, secondBears, hillGiant));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstBears.getId(), secondBears.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstBears.getId(), secondBears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total mana value");
    }

    @Test
    void putsBothCountersOnTheOnlyReturnedCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayPutBothCountersOnTheSameCreatureWhenTwoReturn() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.passBothPriorities();
        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, returnedBears.getId());
        harness.handlePermanentChosen(player1, returnedBears.getId());
        harness.passBothPriorities();

        Permanent returnedElves = findPermanent(player1, "Llanowar Elves");
        assertThat(returnedBears.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(returnedBears.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(returnedElves.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(returnedElves.getCounterCount(CounterType.MENACE)).isZero();
    }

    @Test
    void canChooseZeroTargetsWithoutPuttingCountersOnExistingCreatures() {
        Card elves = new LlanowarElves();
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(existing.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(existing.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canCastWithoutAnyLegalGraveyardTargets() {
        harness.setGraveyard(player1, List.of(new HillGiant()));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Call of the Death-Dweller");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void putsBothCountersOnTheRemainingLegalTarget() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.setGraveyard(player1, List.of(elves));
        harness.setExile(player1, List.of(bears));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotOfferCountersToATargetReturnedByAnotherEffectBeforeResolution() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.setGraveyard(player1, List.of(elves));
        Permanent previouslyReturned = harness.addToBattlefieldAndReturn(player1, bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Permanent returned = findPermanent(player1, "Llanowar Elves");
        assertThat(returned.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(previouslyReturned.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(previouslyReturned.getCounterCount(CounterType.MENACE)).isZero();
    }

    @Test
    void rejectsThreeTargetsEvenWhenTheirTotalManaValueIsThree() {
        Card first = new LlanowarElves();
        Card second = new LlanowarElves();
        Card third = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyOffersCreaturesInTheControllersGraveyard() {
        Card elves = new LlanowarElves();
        Card noncreature = new CallOfTheDeathDweller();
        Card opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(elves, noncreature));
        harness.setGraveyard(player2, List.of(opposingBears));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(elves.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(noncreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingBears.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNothingWhenAllTargetsLeaveTheGraveyardBeforeResolution() {
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(bears, elves));
        harness.setHand(player1, List.of(new CallOfTheDeathDweller()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elves.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears, elves));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(existing);
        assertThat(existing.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(existing.getCounterCount(CounterType.MENACE)).isZero();
        harness.assertInGraveyard(player1, "Call of the Death-Dweller");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
