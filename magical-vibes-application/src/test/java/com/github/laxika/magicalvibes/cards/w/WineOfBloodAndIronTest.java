package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
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

@CardUsed({WineOfBloodAndIron.class, HandOfHonor.class})
class WineOfBloodAndIronTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles the target creature's power and sacrifices at the next end step")
    void boostsTargetAndSacrificesAtNextEndStep() {
        Permanent wine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wine);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wine of Blood and Iron");
        harness.assertInGraveyard(player1, "Wine of Blood and Iron");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses the target creature's power when the ability resolves")
    void evaluatesTargetPowerAtResolution() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        Permanent wine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wine);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());
        Permanent otherWine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherWine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations each use the power at their own resolution")
    void repeatedActivationsCompoundPower() {
        harness.addToBattlefield(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertInGraveyard(player1, "Wine of Blood and Iron");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can boost an opponent's creature during their turn")
    void boostsOpposingCreatureAndSacrificesDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player2, new HandOfHonor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Wine of Blood and Iron");
    }

    @Test
    @DisplayName("An illegal target prevents the delayed sacrifice from being created")
    void illegalTargetDoesNotScheduleSacrifice() {
        harness.addToBattlefield(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Wine of Blood and Iron");
        harness.assertNotInGraveyard(player1, "Wine of Blood and Iron");
    }

    @Test
    @DisplayName("Activation during the end step waits for the following end step")
    void endStepActivationWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.assertOnBattlefield(player1, "Wine of Blood and Iron");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Wine of Blood and Iron");
    }
}
