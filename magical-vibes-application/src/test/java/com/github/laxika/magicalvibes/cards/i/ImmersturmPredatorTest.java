package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmersturmPredator.class, FearlessPup.class})
class ImmersturmPredatorTest extends BaseCardTest {

    @Test
    void becomingTappedExilesAChosenGraveyardCardAndAddsACounter() {
        Permanent predator = addReadyPredator(player1);
        Card card = new FearlessPup();
        harness.setGraveyard(player2, List.of(card));

        tap(predator);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Fearless Pup");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    void becomingTappedAddsACounterEvenWhenNoGraveyardCardIsChosen() {
        Permanent predator = addReadyPredator(player1);
        Card card = new FearlessPup();
        harness.setGraveyard(player2, List.of(card));

        tap(predator);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Fearless Pup");
    }

    @Test
    void sacrificeAbilitySacrificesAnotherCreatureGrantsIndestructibleAndTaps() {
        Permanent predator = addReadyPredator(player1);
        harness.addToBattlefield(player1, new FearlessPup());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(predator.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Fearless Pup");
    }

    @Test
    void cannotActivateSacrificeAbilityWithoutAnotherCreature() {
        addReadyPredator(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void indestructibleWearsOffAtEndOfTurn() {
        Permanent predator = addReadyPredator(player1);
        harness.addToBattlefield(player1, new FearlessPup());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void becomingTappedWithEmptyGraveyardsStillAddsACounter() {
        Permanent predator = addReadyPredator(player1);

        tap(predator);
        harness.passBothPriorities();

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void alreadyTappedPredatorCanGainIndestructibleWithoutTriggeringAgain() {
        Permanent predator = addReadyPredator(player1);
        predator.tap();
        harness.addToBattlefield(player1, new FearlessPup());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Fearless Pup");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void summoningSickPredatorCanActivateItsSacrificeAbility() {
        Permanent predator = harness.addToBattlefieldAndReturn(player1, new ImmersturmPredator());
        harness.addToBattlefield(player1, new FearlessPup());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(predator.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Fearless Pup");
    }
    private Permanent addReadyPredator(Player player) {
        return addCreatureReady(player, new ImmersturmPredator());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
