package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedGoblin.class, BorosRecruit.class})
class FrenziedGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues the trigger for target selection")
    void attackQueuesTargetSelection() {
        addCreatureReady(player1, new FrenziedGoblin());
        addCreatureReady(player2, new BorosRecruit());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Paying {R} makes the target creature unable to block")
    void payingMakesTargetUnableToBlock() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recruit.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Declining the payment leaves the target able to block")
    void decliningLeavesTargetAbleToBlock() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(recruit.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Accepting without red mana leaves the target able to block")
    void cannotPayLeavesTargetAbleToBlock() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recruit.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The restriction wears off at end of turn")
    void restrictionWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(recruit.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(recruit.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Paying {R} can make the attacking player's creature unable to block")
    void payingCanTargetOwnCreature() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player1, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(recruit.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A target that leaves before resolution makes the ability fizzle")
    void targetLeavingBeforeResolutionFizzlesAbility() {
        addCreatureReady(player1, new FrenziedGoblin());
        Permanent recruit = addCreatureReady(player2, new BorosRecruit());
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, recruit));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(recruit.isCantBlockThisTurn()).isFalse();
    }
}
