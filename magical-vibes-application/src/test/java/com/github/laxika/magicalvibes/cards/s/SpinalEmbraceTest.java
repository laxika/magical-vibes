package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpinalEmbraceTest extends BaseCardTest {

    @Test
    @DisplayName("During combat, Spinal Embrace untaps, steals, and grants haste to the target")
    void resolvesCombatControlEffect() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The creature is sacrificed at the next end step and its toughness becomes life gained")
    void sacrificesCreatureAndGainsLifeAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castSpinalEmbrace(target.getId());
        harness.passBothPriorities();
        int lifeBeforeEndStep = gd.getLife(player1.getId());

        drainEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBeforeEndStep + 2);
    }

    @Test
    @DisplayName("Spinal Embrace cannot target a creature already controlled by its caster")
    void cannotTargetOwnCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        setUpSpinalEmbrace();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Spinal Embrace cannot be cast outside combat")
    void cannotCastOutsideCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        setUpSpinalEmbrace();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void setUpSpinalEmbrace() {
        harness.setHand(player1, List.of(new SpinalEmbrace()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void castSpinalEmbrace(java.util.UUID targetId) {
        setUpSpinalEmbrace();
        harness.castInstant(player1, 0, targetId);
    }

    private void drainEndStep() {
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleEndStepTriggers(gd));
    }
}
