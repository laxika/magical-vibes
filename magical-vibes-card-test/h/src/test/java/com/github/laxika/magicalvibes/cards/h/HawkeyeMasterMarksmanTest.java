package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeMasterMarksman.class})
class HawkeyeMasterMarksmanTest extends BaseCardTest {

    @Test
    void paysForAndResolvesThreeDifferentTrickArrows() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        Permanent target = addCreatureReady(player2, new HawkeyeMasterMarksman());
        Card discarded = new HawkeyeMasterMarksman();
        Card drawn = new HawkeyeMasterMarksman();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);

        harness.handleXValueChosen(player1, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Net");
        harness.handleListChoice(player1, "Explosive");
        harness.handleListChoice(player1, "Boomerang");
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void decliningPaymentDoesNothing() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void boomerangDrawsEvenWhenThereIsNoCardToDiscard() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        Card drawn = new HawkeyeMasterMarksman();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Boomerang");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canPayForThreeArrowsButChooseNoModes() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Done");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canChooseOnlyExplosiveAndTargetItsControllerAfterThreePayments() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Explosive");
        harness.handleListChoice(player1, "Done");
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
