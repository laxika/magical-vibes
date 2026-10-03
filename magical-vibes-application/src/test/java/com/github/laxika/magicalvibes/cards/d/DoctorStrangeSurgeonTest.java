package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoctorStrangeSurgeon.class, GrizzlyBears.class, DressDown.class})
class DoctorStrangeSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles the controller's life gain")
    void doublesControllerLifeGain() {
        harness.addToBattlefield(player1, new DoctorStrangeSurgeon());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("At 10 life above starting life, own creatures get +2/+2 and vigilance")
    void boostsOwnCreaturesAtLifeThreshold() {
        harness.setLife(player1, 30);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not boost creatures below the life threshold")
    void doesNotBoostBelowLifeThreshold() {
        harness.setLife(player1, 29);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The combat boost wears off at end of turn")
    void combatBoostWearsOffAtEndOfTurn() {
        harness.setLife(player1, 30);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new DoctorStrangeSurgeon());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    void doesNotDoubleOpponentsLifeGain() {
        harness.addToBattlefield(player1, new DoctorStrangeSurgeon());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player2, 23);
    }

    @Test
    void boostsCreaturesDuringOpponentsCombat() {
        harness.setLife(player1, 30);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void rechecksLifeThresholdOnResolution() {
        harness.setLife(player1, 30);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 29);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void doesNotTriggerWhenLifeThresholdIsOnlyReachedAfterCombatBegins() {
        harness.setLife(player1, 29);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).isEmpty();
        harness.setLife(player1, 30);

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveTheBoost() {
        harness.setLife(player1, 30);
        addCreatureReady(player1, new DoctorStrangeSurgeon());
        advanceToCombatAndResolve(player1);

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void commanderRequiresTenLifeAboveForty() {
        gd.format = DeckFormat.COMMANDER;
        harness.setLife(player1, 40);
        harness.setLife(player2, 40);
        Permanent doctor = addCreatureReady(player1, new DoctorStrangeSurgeon());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, doctor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doctor)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, doctor, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void doesNotDoubleLifeGainWhileItsAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new DoctorStrangeSurgeon());
        harness.addToBattlefield(player2, new DressDown());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 23);
    }

    @Test
    void lifelinkLifeGainIsDoubled() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DoctorStrangeSurgeon());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 18);
    }

    @Test
    void creaturesEnteringBeforeResolutionReceiveTheBoost() {
        harness.setLife(player1, 30);
        addCreatureReady(player1, new DoctorStrangeSurgeon());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
    }
}
