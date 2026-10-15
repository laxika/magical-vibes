package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SageOfFables.class, StonybrookSchoolmaster.class, BurrentonBombardier.class})
class SageOfFablesTest extends BaseCardTest {


    @Test
    @DisplayName("Other Wizard you control enters with an additional +1/+1 counter")
    void wizardEntersWithCounter() {
        addReadySage(player1);

        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = schoolmasterOnBattlefield(player1);
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Wizard creature does not get a counter")
    void nonWizardDoesNotGetCounter() {
        addReadySage(player1);

        harness.setHand(player1, List.of(new BurrentonBombardier()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bombardier = findPermanent(player1, "Burrenton Bombardier");
        assertThat(bombardier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's Wizard does not benefit from your Sage of Fables")
    void opponentWizardDoesNotBenefit() {
        addReadySage(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent wizard = schoolmasterOnBattlefield(player2);
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Two Sages of Fables grant two additional counters to an entering Wizard")
    void twoSagesGrantTwoCounters() {
        addReadySage(player1);
        addReadySage(player1);

        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = schoolmasterOnBattlefield(player1);
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Wizard entering with no other Sage present gets no counter from its own static")
    void loneWizardGetsNoCounter() {
        harness.setHand(player1, List.of(new StonybrookSchoolmaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = schoolmasterOnBattlefield(player1);
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sage of Fables does not put a counter on itself as it enters")
    void sageDoesNotBenefitFromItsOwnStaticAbility() {
        harness.setHand(player1, List.of(new SageOfFables()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sage = findPermanent(player1, "Sage of Fables");
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }


    @Test
    @DisplayName("Ability removes a +1/+1 counter from a creature you control and draws a card")
    void abilityRemovesCounterAndDraws() {
        Permanent sage = addReadySage(player1);
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Ability may remove a +1/+1 counter from another creature you control")
    void abilityRemovesCounterFromAnotherControlledCreature() {
        addReadySage(player1);
        Permanent schoolmaster = addCreatureReady(player1, new StonybrookSchoolmaster());
        schoolmaster.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(schoolmaster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Ability cannot be activated when no creature has a +1/+1 counter")
    void abilityRequiresCounter() {
        addReadySage(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void abilityRequiresMana() {
        Permanent sage = addReadySage(player1);
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }



    @Test
    @DisplayName("A second Sage enters with a counter from the first Sage")
    void secondSageBenefitsFromExistingSage() {
        Permanent first = addReadySage(player1);
        harness.setHand(player1, List.of(new SageOfFables()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Sage of Fables").get(1)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counter payment lets you choose among eligible creatures and happens before drawing")
    void choosesCreatureForCounterPayment() {
        Permanent sage = addReadySage(player1);
        Permanent bombardier = addCreatureReady(player1, new BurrentonBombardier());
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bombardier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, bombardier.getId());

        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bombardier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent's counters cannot pay for the ability")
    void cannotRemoveOpponentsCounter() {
        addReadySage(player1);
        Permanent opponent = addCreatureReady(player2, new BurrentonBombardier());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapped summoning-sick Sage can activate without tapping")
    void tappedSummoningSickSageCanActivate() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfFables());
        sage.setSummoningSick(true);
        sage.tap();
        sage.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(sage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent addReadySage(Player player) {
        return addCreatureReady(player, new SageOfFables());
    }

    private Permanent schoolmasterOnBattlefield(Player player) {
        return findPermanent(player, "Stonybrook Schoolmaster");
    }
}
