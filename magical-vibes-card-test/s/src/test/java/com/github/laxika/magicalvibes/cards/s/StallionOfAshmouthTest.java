package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhoulcallersAccomplice;
import com.github.laxika.magicalvibes.cards.e.ExplosiveApparatus;
import com.github.laxika.magicalvibes.cards.m.MurderousCompulsion;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StallionOfAshmouth.class, GhoulcallersAccomplice.class, Forest.class,
        MurderousCompulsion.class, ExplosiveApparatus.class, DeadWeight.class})
class StallionOfAshmouthTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn with delirium")
    void getsBoostWithDelirium() {
        setDelirium();
        Permanent stallion = addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stallion)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        setDelirium();
        Permanent stallion = addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stallion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without delirium")
    void cannotActivateWithoutDelirium() {
        harness.setGraveyard(player1, List.of(new GhoulcallersAccomplice(), new Forest(), new MurderousCompulsion()));
        addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resolvesEvenIfDeliriumIsLostAfterActivation() {
        setDelirium();
        Permanent stallion = addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stallion)).isEqualTo(4);
        addActivationMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void repeatedActivationsStackWithoutTappingTheStallion() {
        setDelirium();
        Permanent stallion = harness.addToBattlefieldAndReturn(player1, new StallionOfAshmouth());
        stallion.tap();
        prepareActivation();
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stallion)).isEqualTo(5);
    }

    @Test
    void fourCardsWithOnlyThreeCardTypesDoNotEnableActivation() {
        harness.setGraveyard(player1, List.of(new GhoulcallersAccomplice(),
                new StallionOfAshmouth(), new Forest(), new MurderousCompulsion()));
        addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsGraveyardDoesNotEnableActivation() {
        harness.setGraveyard(player2, List.of(new GhoulcallersAccomplice(), new Forest(),
                new MurderousCompulsion(), new ExplosiveApparatus()));
        addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentCountsAsATypeAndAbilityCanBeUsedOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new GhoulcallersAccomplice(), new Forest(),
                new DeadWeight(), new ExplosiveApparatus()));
        Permanent stallion = addCreatureReady(player1, new StallionOfAshmouth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stallion)).isEqualTo(4);
    }

    @Test
    void deliriumDoesNotWaiveTheManaCost() {
        setDelirium();
        Permanent stallion = addCreatureReady(player1, new StallionOfAshmouth());
        prepareActivation();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.getEffectivePower(gd, stallion)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GhoulcallersAccomplice(), new Forest(), new MurderousCompulsion(), new ExplosiveApparatus()));
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
