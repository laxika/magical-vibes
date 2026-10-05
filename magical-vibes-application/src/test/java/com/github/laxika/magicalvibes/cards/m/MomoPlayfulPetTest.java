package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    @Test
    void foodCanBeSacrificedForThreeLifeImmediately() {
        removeMomo();
        harness.handleListChoice(player1, "Create a Food token.");
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Food");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 13);
    }

    @Test
    void createsFoodWhenReturnedToHand() {
        Permanent momo = harness.addToBattlefieldAndReturn(player1, new MomoPlayfulPet());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToHand(gd, momo));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Food token.");
        harness.passBothPriorities();
        harness.assertInHand(player1, "Momo, Playful Pet");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void createsFoodWhenExiled() {
        Permanent momo = harness.addToBattlefieldAndReturn(player1, new MomoPlayfulPet());
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToExile(gd, momo));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Food token.");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Momo, Playful Pet");
        harness.assertNotInGraveyard(player1, "Momo, Playful Pet");
        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    void opposingCreatureDoesNotMakeCounterModeLegal() {
        harness.addToBattlefield(player2, new MomoPlayfulPet());
        removeMomo();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("Create a Food token.", "Scry 2.");
        harness.handleListChoice(player1, "Create a Food token.");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Food");
        assertThat(findPermanent(player2, "Momo, Playful Pet")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
