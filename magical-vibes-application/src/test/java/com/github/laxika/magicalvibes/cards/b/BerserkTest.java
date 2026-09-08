package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Berserk.class, FountainOfYouth.class, GrizzlyBears.class})
class BerserkTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature trample and +X/+0 where X is its power")
    void grantsTrampleAndPowerBasedBoost() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castBerserk(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Destroys the target at the next end step if it attacked this turn")
    void destroysTargetThatAttacked() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castBerserk(target);

        declareAttackers(player1, List.of(0));
        runEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy the target if it did not attack this turn")
    void sparesTargetThatDidNotAttack() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castBerserk(target);

        runEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot be cast once the combat damage step is reached")
    void cannotCastAtCombatDamage() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void castBerserk(Permanent target) {
        harness.setHand(player1, List.of(new Berserk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void runEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(StepTriggerService.class).handleEndStepTriggers(gd));
    }
}
