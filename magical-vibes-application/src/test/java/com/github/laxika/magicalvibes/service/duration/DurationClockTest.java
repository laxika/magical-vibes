package com.github.laxika.magicalvibes.service.duration;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.rules.ActionEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.rules.StepEvent;
import com.github.laxika.magicalvibes.model.rules.TurnEvent;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DurationClockTest extends BaseCardTest {

    private FloatingContinuousEffect add(UUID controllerId, Duration duration) {
        return gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Test", null, controllerId,
                new BoostTargetCreatureEffect(1, 1), null, null, null, duration, 0));
    }

    private List<FloatingContinuousEffect> advance(RulesEvent event) {
        return GameTestEngineContext.get().getBean(DurationClock.class).advance(gd, event);
    }

    @Test
    @DisplayName("A turn beginning ends only that player's \"until your next turn\" effects")
    void turnBeginningEndsThatPlayersEffects() {
        FloatingContinuousEffect own = add(player1.getId(), Duration.UNTIL_YOUR_NEXT_TURN);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_YOUR_NEXT_TURN);
        FloatingContinuousEffect endOfTurn = add(player1.getId(), Duration.UNTIL_END_OF_TURN);

        assertThat(advance(new TurnEvent.Began(player1.getId()))).containsExactly(own);
        assertThat(gd.floatingEffects).containsExactly(opponents, endOfTurn);
    }

    @Test
    @DisplayName("The combat phase ending ends \"until end of combat\" effects; another step ending ends none")
    void combatEndingEndsEndOfCombatEffects() {
        FloatingContinuousEffect combat = add(player1.getId(), Duration.UNTIL_END_OF_COMBAT);

        assertThat(advance(new TurnEvent.StepEnded(TurnStep.DECLARE_BLOCKERS, player1.getId()))).isEmpty();
        assertThat(advance(new TurnEvent.StepEnded(TurnStep.END_OF_COMBAT, player1.getId()))).containsExactly(combat);
    }

    @Test
    @DisplayName("An upkeep ending ends its player's effects that last through the end of their next upkeep (CR 500.5)")
    void upkeepEndingEndsEffectsBegunBeforeIt() {
        FloatingContinuousEffect before = add(player1.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);
        gd.upkeepStartTimestamp = gd.timestampCounter + 1;
        FloatingContinuousEffect during = add(player1.getId(), Duration.UNTIL_END_OF_CONTROLLERS_NEXT_UPKEEP);

        assertThat(advance(new TurnEvent.StepEnded(TurnStep.UPKEEP, player1.getId()))).containsExactly(before);
        assertThat(gd.floatingEffects).containsExactly(opponents, during);
    }

    @Test
    @DisplayName("The cleanup step ends \"until end of turn\" effects and the active player's \"until the end of your"
            + " next turn\" effects begun before this turn (CR 514.2)")
    void cleanupEndsEndOfTurnEffects() {
        FloatingContinuousEffect endOfTurn = add(player2.getId(), Duration.UNTIL_END_OF_TURN);
        FloatingContinuousEffect lastTurns = add(player1.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        gd.turnStartTimestamp = gd.timestampCounter + 1;
        FloatingContinuousEffect thisTurns = add(player1.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_END_OF_YOUR_NEXT_TURN);
        FloatingContinuousEffect lasting = add(player1.getId(), Duration.WHILE_SOURCE_ON_BATTLEFIELD);

        assertThat(advance(new TurnEvent.Cleanup(player1.getId()))).containsExactly(endOfTurn, lastTurns);
        assertThat(gd.floatingEffects).containsExactly(thisTurns, opponents, lasting);
    }

    @Test
    @DisplayName("An upkeep beginning ends its player's \"until your next upkeep\" effects")
    void upkeepBeginningEndsThatPlayersEffects() {
        FloatingContinuousEffect own = add(player1.getId(), Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP);
        FloatingContinuousEffect opponents = add(player2.getId(), Duration.UNTIL_CONTROLLERS_NEXT_UPKEEP);

        assertThat(advance(new StepEvent(StepTrigger.Moment.UPKEEP, player1.getId()))).containsExactly(own);
        assertThat(gd.floatingEffects).containsExactly(opponents);
    }

    @Test
    @DisplayName("The end step beginning ends the effects that last until the next end step")
    void endStepEndsNextEndStepEffects() {
        FloatingContinuousEffect endStep = add(player2.getId(), Duration.UNTIL_NEXT_END_STEP);

        assertThat(advance(new StepEvent(StepTrigger.Moment.END_STEP, player1.getId()))).containsExactly(endStep);
    }

    @Test
    @DisplayName("A creature spell being cast ends the effects that last until a creature spell is cast")
    void creatureCastEndsCreatureCastEffects() {
        FloatingContinuousEffect creatureCast = add(player2.getId(), Duration.UNTIL_CREATURE_SPELL_CAST);

        assertThat(advance(new SpellEvent.Cast(new GrizzlyBears(), player1.getId(), Zone.HAND, null, false, false,
                0, 0, 0, 0))).containsExactly(creatureCast);
    }

    @Test
    @DisplayName("Planeswalking ends the effects that last until a player planeswalks")
    void planeswalkEndsPlaneswalkEffects() {
        FloatingContinuousEffect planar = add(player1.getId(), Duration.UNTIL_PLANESWALK);

        assertThat(advance(new ActionEvent.Planeswalked(player1.getId()))).containsExactly(planar);
    }

    @Test
    @DisplayName("An ending \"until end of turn\" copy effect reverts the permanent to its card before the copy")
    void endingCopyEffectRevertsTheCopy() {
        Permanent skinshifter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card bears = skinshifter.getCard();
        skinshifter.setPreCopyCard(bears);
        skinshifter.setCard(new SerraAngel());
        skinshifter.setCopyUntilEndOfTurn(true);
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Tilonalli's Skinshifter",
                skinshifter.getId(), player1.getId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                skinshifter.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        advance(new TurnEvent.Cleanup(player1.getId()));

        assertThat(skinshifter.getCard()).isSameAs(bears);
        assertThat(skinshifter.isCopyUntilEndOfTurn()).isFalse();
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("An ending copy effect whose permanent left the battlefield just ends")
    void endingCopyEffectOfDepartedPermanent() {
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Tilonalli's Skinshifter",
                UUID.randomUUID(), player1.getId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                UUID.randomUUID(), null, null, Duration.UNTIL_END_OF_TURN, 0));

        assertThat(advance(new TurnEvent.Cleanup(player1.getId()))).hasSize(1);
        assertThat(gd.floatingEffects).isEmpty();
    }

    @Test
    @DisplayName("The trigger pipeline runs the clock on the events it is handed")
    void pipelineRunsTheClock() {
        add(player1.getId(), Duration.UNTIL_END_OF_TURN);
        FloatingContinuousEffect lasting = add(player1.getId(), Duration.PERMANENT);

        gd.rulesEvents.occur(new TurnEvent.Cleanup(player1.getId()));

        assertThat(gd.floatingEffects).containsExactly(lasting);
    }
}
