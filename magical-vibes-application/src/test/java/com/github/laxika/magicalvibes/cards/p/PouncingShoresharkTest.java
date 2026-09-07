package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PouncingShoreshark.class, GrizzlyBears.class})
class PouncingShoresharkTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating may return a creature an opponent controls to its owner's hand")
    void mutatingMayReturnOpponentsCreatureToHand() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the mutation trigger leaves the opponent's creature on the battlefield")
    void decliningMutationTriggerDoesNotReturnCreature() {
        Permanent shark = addCreatureReady(player1, new PouncingShoreshark());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(shark);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void triggerMutation(Permanent shark) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, shark, List.of(shark.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
