package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Soulherder.class, GrizzlyBears.class})
class SoulherderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when any creature is exiled from the battlefield")
    void growsWhenOpponentCreatureIsExiled() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToExile(bears);

        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("May flicker another creature you control at your end step")
    void mayFlickerAnotherCreatureAtEndStep() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bearsId);
        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Cannot target Soulherder itself or an opponent's creature")
    void targetMustBeAnotherCreatureYouControl() {
        harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1,
                harness.getPermanentId(player1, "Soulherder")))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
