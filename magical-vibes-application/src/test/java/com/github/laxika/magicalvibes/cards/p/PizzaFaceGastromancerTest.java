package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FrogButler;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.cards.o.OmniCheesePizza;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PizzaFaceGastromancer.class, OmniCheesePizza.class, FrogButler.class, MouserMarkIII.class})
class PizzaFaceGastromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and creates a Food token")
    void createsFoodOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Food");
    }

    @Test
    @DisplayName("At your end step, puts counters on and animates another artifact")
    void putsCountersOnAndAnimatesAnotherArtifact() {
        Permanent pizzaFace = harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new FrogButler());
        remove(leaving);

        resolveEndStepTargeting(artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.MUTANT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pizzaFace);
    }

    @Test
    @DisplayName("At your end step, a creature gets counters without animation")
    void putsCountersOnCreatureWithoutAnimation() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FrogButler());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new MouserMarkIII());
        remove(leaving);

        resolveEndStepTargeting(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.MUTANT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The end-step ability does not trigger when only an opponent's permanent left")
    void doesNotTriggerForOpponentsPermanent() {
        Permanent pizzaFace = harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MouserMarkIII());
        Permanent leaving = harness.addToBattlefieldAndReturn(player2, new FrogButler());
        remove(leaving);

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pizzaFace);
    }

    @Test
    @DisplayName("Sacrificing Pizza Face gains 15 life")
    void sacrificeAbilityGainsLife() {
        addCreatureReady(player1, new PizzaFaceGastromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 15);
        harness.assertNotOnBattlefield(player1, "Pizza Face, Gastromancer");
    }

    @Test
    void cannotActivateSacrificeAbilityWhileTapped() {
        Permanent pizzaFace = addCreatureReady(player1, new PizzaFaceGastromancer());
        pizzaFace.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Pizza Face, Gastromancer");
    }

    @Test
    void cannotActivateSacrificeAbilityWhileSummoningSick() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Pizza Face, Gastromancer");
    }

    @Test
    void canDeclineEndStepTargetAndCannotTargetItself() {
        Permanent pizzaFace = harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());
        remove(harness.addToBattlefieldAndReturn(player1, new FrogButler()));

        advanceToEndStep();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(artifact.getId(), player1.getId())
                .doesNotContain(pizzaFace.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
    }

    @Test
    void canAnimateOpponentsArtifactAfterOwnArtifactLeaves() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OmniCheesePizza());
        remove(harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza()));

        resolveEndStepTargeting(artifact);

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.MUTANT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
    }

    @Test
    void artifactCreatureKeepsItsBaseStatsAndSubtypes() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MouserMarkIII());
        remove(harness.addToBattlefieldAndReturn(player1, new FrogButler()));

        resolveEndStepTargeting(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ROBOT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.MUTANT)).isFalse();
    }

    @Test
    void foodTokenCanBeSacrificedForThreeLife() {
        harness.enterBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);

        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        harness.assertNotOnBattlefield(player1, "Food");
    }

    @Test
    void noEndStepTriggerWithoutAnyDeparture() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void removedTargetDoesNotReceiveCountersOrReturn() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());
        remove(harness.addToBattlefieldAndReturn(player1, new FrogButler()));
        advanceToEndStep();
        harness.handlePermanentChosen(player1, artifact.getId());

        remove(artifact);
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotOnBattlefield(player1, "Omni-Cheese Pizza");
        harness.assertInGraveyard(player1, "Omni-Cheese Pizza");
    }

    @Test
    void animationPersistsAfterSourceLeavesAndTurnEnds() {
        Permanent pizzaFace = harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new OmniCheesePizza());
        remove(harness.addToBattlefieldAndReturn(player1, new FrogButler()));
        resolveEndStepTargeting(artifact);

        remove(pizzaFace);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isArtifact(gd, artifact)).isTrue();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.FOOD)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, artifact, CardSubtype.MUTANT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, artifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, artifact)).isEqualTo(3);
    }

    private void resolveEndStepTargeting(Permanent target) {
        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    private void remove(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
