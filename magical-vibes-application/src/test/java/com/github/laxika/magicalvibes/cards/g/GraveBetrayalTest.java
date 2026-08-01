package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GraveBetrayalTest extends BaseCardTest {

    /** Player1 shocks player2's Grizzly Bears to death in player1's precombat main phase. */
    private void shockOpponentBearsToDeath() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Shock resolves, Bears dies
        harness.passBothPriorities(); // Grave Betrayal's trigger resolves, scheduling the return
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }

    @Test
    @DisplayName("An opponent's dying creature returns under your control at the next end step with a +1/+1 counter")
    void returnsOpponentCreatureAtEndStepWithCounter() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();

        // Nothing happens right away — the return is delayed to the beginning of the next end step.
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("The returned creature is a black Zombie in addition to its other colors and types")
    void returnedCreatureIsBlackZombie() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getGrantedColors()).contains(CardColor.BLACK);
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("A creature you control dying does not trigger Grave Betrayal")
    void ownCreatureDoesNotReturn() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();

        advanceToEndStep();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature that leaves the graveyard before the end step does not return")
    void doesNotReturnIfCardLeftGraveyard() {
        harness.addToBattlefield(player1, new GraveBetrayal());
        shockOpponentBearsToDeath();

        // The card is exiled from the graveyard in response — nothing is there to return.
        gd.playerGraveyards.get(player2.getId()).clear();

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
    }
}
