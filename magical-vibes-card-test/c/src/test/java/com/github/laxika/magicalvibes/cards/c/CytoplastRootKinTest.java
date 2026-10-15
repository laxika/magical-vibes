package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.s.SimicAscendancy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CytoplastRootKin.class, MistralCharger.class, Solemnity.class, SimicAscendancy.class})
class CytoplastRootKinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        Permanent rootKin = castRootKin();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Its enter-the-battlefield ability adds counters to other countered creatures you control")
    void entersAndAddsCountersToOtherCounteredCreatures() {
        Permanent counteredCreature = addCreatureReady(player1, new MistralCharger());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent uncounteredCreature = addCreatureReady(player1, new MistralCharger());
        Permanent opponentCreature = addCreatureReady(player2, new MistralCharger());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent rootKin = castRootKin();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(counteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(uncounteredCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent rootKin = castRootKin();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature that enters")
    void graftMovesCounterOntoOpponentsEnteringCreature() {
        Permanent rootKin = castRootKin();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent rootKin = castRootKin();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability moves a counter from a creature you control onto Cytoplast Root-Kin")
    void activatedAbilityMovesCounterOntoSource() {
        Permanent rootKin = castRootKin();
        rootKin.setSummoningSick(false);
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                charger.getId());
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability does nothing when the target has no +1/+1 counter")
    void activatedAbilityDoesNothingWhenTargetHasNoCounter() {
        Permanent rootKin = castRootKin();
        rootKin.setSummoningSick(false);
        Permanent charger = addCreatureReady(player1, new MistralCharger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                charger.getId());
        harness.passBothPriorities();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target an opponent's creature")
    void activatedAbilityCannotTargetOpponentsCreature() {
        Permanent rootKin = castRootKin();
        rootKin.setSummoningSick(false);
        Permanent opponentCreature = addCreatureReady(player2, new MistralCharger());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graft leaves the source counter intact when the entering creature cannot receive counters")
    void graftDoesNotRemoveCounterWhenPlacementIsForbidden() {
        Permanent rootKin = castRootKin();
        harness.castFromHand(player1, new Solemnity(), "{2}{W}");
        resolveAllTriggers();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The activated ability leaves the target counter intact when Root-Kin cannot receive counters")
    void activatedAbilityDoesNotRemoveCounterWhenPlacementIsForbidden() {
        Permanent rootKin = castRootKin();
        Permanent charger = addCreatureReady(player1, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.castFromHand(player1, new Solemnity(), "{2}{W}");
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                charger.getId());
        resolveAllTriggers();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Self-targeting while summoning sick moves no counter and triggers no counter-placement ability")
    void activatedAbilityCanTargetItselfWhileSummoningSick() {
        Permanent rootKin = castRootKin();
        harness.castFromHand(player1, new SimicAscendancy(), "{G}{U}");
        resolveAllTriggers();
        Permanent ascendancy = findPermanent(player1, "Simic Ascendancy");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(rootKin),
                null,
                rootKin.getId());
        resolveAllTriggers();

        assertThat(rootKin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(ascendancy.getCounterCount(CounterType.GROWTH)).isZero();
    }

    private Permanent castRootKin() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CytoplastRootKin(), "{2}{G}{G}");
        resolveAllTriggers();
        return findPermanent(player1, "Cytoplast Root-Kin");
    }
}
