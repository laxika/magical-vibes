package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Soulherder.class, GrizzlyBears.class, Forest.class})
class SoulherderTest extends BaseCardTest {

    @Test
    @DisplayName("A creature exiled from either battlefield puts a counter on Soulherder")
    void creatureExiledFromEitherBattlefieldPutsCounterOnSoulherder() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        removeToExile(opponentCreature);

        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Exiling a noncreature permanent does not trigger Soulherder")
    void noncreatureExiledDoesNotTriggerSoulherder() {
        Permanent soulherder = harness.addToBattlefieldAndReturn(player1, new Soulherder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToExile(forest);

        assertThat(soulherder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("At your end step, Soulherder may flicker another creature you control")
    void endStepMayFlickerAnotherCreatureYouControl() {
        harness.addToBattlefield(player1, new Soulherder());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID oldBearsId = bears.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, oldBearsId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.getId()).isNotEqualTo(oldBearsId);
        assertThat(findPermanent(player1, "Soulherder")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Soulherder cannot target itself or an opponent's creature")
    void endStepDoesNotOfferAnIllegalTarget() {
        harness.addToBattlefield(player1, new Soulherder());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
