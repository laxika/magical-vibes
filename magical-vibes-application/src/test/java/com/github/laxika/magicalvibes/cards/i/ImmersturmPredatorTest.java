package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({ImmersturmPredator.class, GrizzlyBears.class, LightningBolt.class})
class ImmersturmPredatorTest extends BaseCardTest {

    @Test
    void becomingTappedExilesAChosenGraveyardCardAndAddsACounter() {
        Permanent predator = addReadyPredator(player1);
        Card card = new LightningBolt();
        harness.setGraveyard(player2, List.of(card));

        tap(predator);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
    }

    @Test
    void becomingTappedAddsACounterEvenWhenNoGraveyardCardIsChosen() {
        Permanent predator = addReadyPredator(player1);
        Card card = new LightningBolt();
        harness.setGraveyard(player2, List.of(card));

        tap(predator);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(predator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    void sacrificeAbilitySacrificesAnotherCreatureGrantsIndestructibleAndTaps() {
        Permanent predator = addReadyPredator(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(predator.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
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
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, predator, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private Permanent addReadyPredator(Player player) {
        Permanent predator = new Permanent(new ImmersturmPredator());
        predator.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(predator);
        return predator;
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
