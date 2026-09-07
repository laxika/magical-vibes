package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MomoPlayfulPet.class, GrizzlyBears.class})
class MomoPlayfulPetTest extends BaseCardTest {

    @Test
    void createsFoodWhenItLeaves() {
        removeMomo();

        harness.handleListChoice(player1, "Create a Food token.");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void putsCounterOnTargetCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        removeMomo();

        harness.handleListChoice(player1, "Put a +1/+1 counter on target creature you control.");
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void scriesTwoWhenChosen() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        removeMomo();

        harness.handleListChoice(player1, "Scry 2.");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(2);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    void doesNotOfferTargetModeWithoutLegalTarget() {
        removeMomo();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("Create a Food token.", "Scry 2.");
    }

    private void removeMomo() {
        Permanent momo = harness.addToBattlefieldAndReturn(player1, new MomoPlayfulPet());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, momo));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
