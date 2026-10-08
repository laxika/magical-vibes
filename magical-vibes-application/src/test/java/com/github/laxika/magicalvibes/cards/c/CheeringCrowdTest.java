package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CheeringCrowd.class, Solemnity.class, VorinclexMonstrousRaider.class})
class CheeringCrowdTest extends BaseCardTest {

    @Test
    @DisplayName("The active player may add a counter and then receives mana equal to the counters")
    void activePlayerMayAddCounterAndMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the trigger does not add a counter or mana")
    void activePlayerMayDecline() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The controller may add the first counter and receives one mana on their turn")
    void controllerMayAddFirstCounterAndMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Mana counts every kind of counter, including stun and keyword counters")
    void manaCountsAllCounterTypes() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        crowd.setCounterCount(CounterType.STUN, 2);
        crowd.setCounterCount(CounterType.FLYING, 1);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(crowd.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(crowd.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining with existing counters still awards no mana")
    void decliningWithExistingCountersAwardsNoMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @CardUsed({CheeringCrowd.class, Solemnity.class})
    @DisplayName("An impossible counter placement under Solemnity cannot award mana")
    void prohibitedCounterPlacementAwardsNoMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new Solemnity());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player2, true);
        }

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @CardUsed({CheeringCrowd.class, VorinclexMonstrousRaider.class})
    @DisplayName("The active player places the counter for Vorinclex's replacement effect")
    void activePlayerPlacesCounterForReplacementEffects() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        harness.addToBattlefield(player2, new VorinclexMonstrousRaider());

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("The postcombat main phase does not trigger the ability")
    void doesNotTriggerDuringPostcombatMain() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @CardUsed({CheeringCrowd.class, VorinclexMonstrousRaider.class})
    @DisplayName("A legal counter payment replaced with zero still awards mana for the existing counters")
    void replacementReducingCounterPaymentToZeroStillAwardsMana() {
        Permanent crowd = harness.addToBattlefieldAndReturn(player1, new CheeringCrowd());
        crowd.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new VorinclexMonstrousRaider());
        advanceToPrecombatMain(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(crowd.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
