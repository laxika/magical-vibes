package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({PizzaFaceGastromancer.class, DarksteelRelic.class, GrizzlyBears.class, Memnite.class})
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new Memnite());
        remove(leaving);

        resolveEndStepTargeting(creature);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.MUTANT)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The end-step ability does not trigger when only an opponent's permanent left")
    void doesNotTriggerForOpponentsPermanent() {
        Permanent pizzaFace = harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Memnite());
        Permanent leaving = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        remove(leaving);

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(pizzaFace);
    }

    @Test
    @DisplayName("Sacrificing Pizza Face gains 15 life")
    void sacrificeAbilityGainsLife() {
        harness.addToBattlefieldAndReturn(player1, new PizzaFaceGastromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 15);
        harness.assertNotOnBattlefield(player1, "Pizza Face, Gastromancer");
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void remove(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
    }
}
