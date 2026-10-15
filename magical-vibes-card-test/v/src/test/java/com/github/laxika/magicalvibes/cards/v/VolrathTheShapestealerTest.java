package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BurnishedHart;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathTheShapestealer.class, BurnishedHart.class, Forest.class, Willbender.class})
class VolrathTheShapestealerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on up to one target creature at combat")
    void putsMinusOneCounterAtBeginningOfCombat() {
        addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new BurnishedHart());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Becomes a 7/5 copy while retaining its activated ability")
    void becomesCopyWithExceptionAndRetainedAbility() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new BurnishedHart());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Burnished Hart");
        assertThat(volrath.getCard().getPower()).isEqualTo(7);
        assertThat(volrath.getCard().getToughness()).isEqualTo(5);
        assertThat(volrath.getCard().getActivatedAbilities()).hasSize(2);
    }

    @Test
    @DisplayName("Copy reverts at the beginning of Volrath's next turn")
    void copyRevertsAtNextTurn() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent bears = addCreatureReady(player2, new BurnishedHart());
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(volrath.getCard().getName()).isEqualTo("Burnished Hart");

        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(volrath.getCard().getName()).isEqualTo("Volrath, the Shapestealer");
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature permanent")
    void activatedAbilityRejectsNoncreatureTarget() {
        addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityRejectsCreatureWithoutCounters() {
        addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingLastCounterBeforeResolutionMakesTargetIllegal() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Volrath, the Shapestealer");
    }

    @Test
    void anyCounterQualifiesAndSourceCountersRemain() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        volrath.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new BurnishedHart());
        target.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Burnished Hart");
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, volrath)).isEqualTo(4);
        assertThat(volrath.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void copiedCreatureCanUseRetainedCopyAbilityAgain() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent other = addCreatureReady(player2, new Willbender());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, other.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCard().getName()).isEqualTo("Willbender");
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, volrath)).isEqualTo(5);
    }

    @Test
    void copyingFaceDownCreatureDoesNotCopyItsHiddenFace() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new Willbender());
        target.setFaceDown(2, 2, java.util.Set.of(CardType.CREATURE));
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(volrath.isFaceDown()).isFalse();
        assertThat(volrath.getCard().getName()).isEmpty();
        assertThat(volrath.getCard().getManaCost()).isNullOrEmpty();
        assertThat(volrath.getCard().getSubtypes()).isEmpty();
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, volrath)).isEqualTo(5);
    }

    @Test
    void combatTriggerDoesNotTriggerOnOpponentsTurn() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(volrath.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void copyingBeforeCombatLosesCounterTrigger() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(volrath.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void combatTriggerCanChooseNoTarget() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());
        Permanent target = addCreatureReady(player2, new BurnishedHart());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void combatTriggerCanTargetVolrathItself() {
        Permanent volrath = addCreatureReady(player1, new VolrathTheShapestealer());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, volrath.getId());
        harness.passBothPriorities();

        assertThat(volrath.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, volrath)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, volrath)).isEqualTo(4);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
