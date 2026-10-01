package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MistralCharger;
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

@CardUsed({CytoplastManipulator.class, MistralCharger.class})
class CytoplastManipulatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        Permanent manipulator = castManipulator();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Graft moves a counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent manipulator = castManipulator();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void mayDeclineGraft() {
        Permanent manipulator = castManipulator();

        harness.castFromHand(player1, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player1, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller of Cytoplast Manipulator chooses graft for an opponent's entering creature")
    void controllerChoosesGraftForOpponentsCreature() {
        Permanent manipulator = castManipulator();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new MistralCharger(), "{1}{W}");
        harness.passBothPriorities();
        Permanent charger = findPermanent(player2, "Mistral Charger");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(manipulator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability steals a creature with a +1/+1 counter until the source leaves")
    void stealsCreatureUntilSourceLeaves() {
        Permanent charger = addCreatureReady(player2, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent manipulator = castManipulator();
        manipulator.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null,
                charger.getId());
        harness.passBothPriorities();

        assertThat(manipulator.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(charger);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manipulator));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(charger);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(charger);
    }

    @Test
    @DisplayName("The activated ability fizzles if the target loses its +1/+1 counter before resolution")
    void targetMustStillHaveCounterOnResolution() {
        Permanent charger = addCreatureReady(player2, new MistralCharger());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent manipulator = castManipulator();
        manipulator.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null,
                charger.getId());
        charger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(charger);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(charger);
    }

    @Test
    @DisplayName("The activated ability cannot target a creature without a +1/+1 counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent charger = addCreatureReady(player2, new MistralCharger());
        Permanent manipulator = castManipulator();
        manipulator.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castManipulator() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CytoplastManipulator(), "{2}{U}{U}");
        harness.passBothPriorities();
        return findPermanent(player1, "Cytoplast Manipulator");
    }
}
