package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MaladyInvoker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerbologyInstructor.class, MaladyInvoker.class, HaloChargedSkaab.class})
class HerbologyInstructorTest extends BaseCardTest {

    @Test
    void entersAndGainsThreeLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new HerbologyInstructor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
    }

    @Test
    void transformsAndGivesOpponentCreatureNegativeToughnessEqualToItsPower() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(instructor.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void transformTriggerCannotTargetYourOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayTwoLifeAndTransformWithoutAnOpponentCreature() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        prepareMainPhase(player1);
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 8);
        harness.passBothPriorities();

        assertThat(instructor.isTransformed()).isTrue();
        harness.assertLife(player1, 8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformCannotBeActivatedOutsideAMainPhase() {
        harness.addToBattlefield(player1, new HerbologyInstructor());
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformCannotBeActivatedDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new HerbologyInstructor());
        prepareMainPhase(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void triggerUsesPowerAtResolutionAndCanKillTheTarget() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        instructor.setPowerModifier(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Halo-Charged Skaab");
        harness.assertInGraveyard(player2, "Halo-Charged Skaab");
    }

    @Test
    void negativeSourcePowerDoesNotIncreaseTargetToughness() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        instructor.setPowerModifier(-4);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void toughnessReductionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new HerbologyInstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void triggerUsesLastKnownPowerAfterSourceDies() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new HerbologyInstructor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        instructor.setPowerModifier(1);
        instructor.setToughnessModifier(-3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Malady Invoker");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Halo-Charged Skaab");
    }

    @Test
    void transformCannotBeActivatedWhileAnotherAbilityIsOnTheStack() {
        harness.addToBattlefield(player1, new HerbologyInstructor());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownCreatureIsExcludedEvenWhenOpponentCreatureIsAvailable() {
        harness.addToBattlefield(player1, new HerbologyInstructor());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HaloChargedSkaab());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HaloChargedSkaab());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
