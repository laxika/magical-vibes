package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyeMasterMarksman.class, FountainOfYouth.class, GrizzlyBears.class})
class HawkeyeMasterMarksmanTest extends BaseCardTest {

    @Test
    void paysForAndResolvesThreeDifferentTrickArrows() {
        Permanent hawkeye = addCreatureReady(player1, new HawkeyeMasterMarksman());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card discarded = new FountainOfYouth();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        tapAndQueueTrigger(hawkeye);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue())
                .isEqualTo(3);

        harness.handleXValueChosen(player1, 3);
        harness.passBothPriorities();
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

    private void tapAndQueueTrigger(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
