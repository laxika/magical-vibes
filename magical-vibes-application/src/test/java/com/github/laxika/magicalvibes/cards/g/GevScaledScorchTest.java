package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GevScaledScorch.class, GrizzlyBears.class, LagacLizard.class})
class GevScaledScorchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives another creature one +1/+1 counter when an opponent lost life")
    void givesOtherCreatureCountersBasedOnOpponentsWhoLostLife() {
        addReadyGev();
        gd.lifeLostThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not give counters when no opponent lost life")
    void doesNotGiveCountersWithoutOpponentLifeLoss() {
        addReadyGev();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Deals 1 damage to a target opponent when a Lizard is cast")
    void damagesTargetOpponentWhenLizardIsCast() {
        addReadyGev();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LagacLizard()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
    }

    private Permanent addReadyGev() {
        Permanent gev = harness.addToBattlefieldAndReturn(player1, new GevScaledScorch());
        gev.setSummoningSick(false);
        return gev;
    }
}
