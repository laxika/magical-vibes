package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WanderwineDistracterTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped queues a target choice and gives an opponent creature -3/-0")
    void becomingTappedDebuffsOpponentCreature() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(distracter);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(-3);
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Tapping another permanent you control does not trigger")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new WanderwineDistracter());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(otherCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent distracter = harness.addToBattlefieldAndReturn(player1, new WanderwineDistracter());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        tap(distracter);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
