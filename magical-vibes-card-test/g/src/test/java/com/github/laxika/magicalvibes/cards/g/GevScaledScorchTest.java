package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.r.RavineRaider;
import com.github.laxika.magicalvibes.cards.s.Savor;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
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

@CardUsed({GevScaledScorch.class, GrizzlyBears.class, LagacLizard.class, RavineRaider.class,
        Savor.class, WitnessProtection.class})
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

        harness.assertLife(player2, 19);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lagac Lizard");
        assertThat(findPermanent(player1, "Lagac Lizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void gevDoesNotGiveItselfCountersOrTriggerForItsOwnCast() {
        gd.lifeLostThisTurn.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new GevScaledScorch()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Gev, Scaled Scorch").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void controllerLifeLossDoesNotGiveCountersOrTriggerForNonLizard() {
        addReadyGev();
        gd.lifeLostThisTurn.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingLizardDoesNotTriggerOrReceiveCounters() {
        addReadyGev();
        gd.lifeLostThisTurn.put(player2.getId(), 3);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new RavineRaider()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Ravine Raider").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void wardCountersOpposingSpellWhenLifePaymentIsDeclined() {
        Permanent gev = addReadyGev();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, gev.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Gev, Scaled Scorch");
        harness.assertInGraveyard(player2, "Savor");
        harness.assertNotOnBattlefield(player2, "Food");
        harness.assertLife(player2, 20);
    }

    @Test
    void wardAllowsOpposingSpellWhenTwoLifeIsPaid() {
        Permanent gev = addReadyGev();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Savor()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, gev.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gev, Scaled Scorch");
        harness.assertOnBattlefield(player2, "Food");
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyGev() {
        return harness.addToBattlefieldAndReturn(player1, new GevScaledScorch());
    }

    @Test
    void losingAbilitiesStopsEntryCountersAndCastTrigger() {
        Permanent gev = addReadyGev();
        harness.setHand(player1, List.of(new WitnessProtection(), new RavineRaider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, gev.getId());
        harness.passBothPriorities();
        gd.lifeLostThisTurn.put(player2.getId(), 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Ravine Raider").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }
}
