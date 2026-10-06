package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirkAlarmist.class, AvenEnvoy.class, Skinthinner.class})
class SkirkAlarmistTest extends BaseCardTest {

    @Test
    void turnsFaceDownCreatureFaceUpAndSacrificesItAtNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new AvenEnvoy());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Aven Envoy");

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Aven Envoy");
        harness.assertInGraveyard(player1, "Aven Envoy");
    }

    @Test
    void canTargetOnlyFaceDownCreaturesYouControl() {
        addCreatureReady(player1, new SkirkAlarmist());
        Permanent faceUp = addCreatureReady(player1, new AvenEnvoy());
        Permanent opponentCreature = addCreatureReady(player2, new AvenEnvoy());
        opponentCreature.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, faceUp.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fizzlesWithoutSchedulingSacrificeIfTargetIsNoLongerFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new AvenEnvoy());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        target.turnFaceUp();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aven Envoy");
        harness.assertNotInGraveyard(player1, "Aven Envoy");
    }

    @Test
    void doesNotSacrificeCreatureThatOpponentControlsAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new AvenEnvoy());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Aven Envoy");
        harness.assertNotInGraveyard(player1, "Aven Envoy");
        harness.assertNotInGraveyard(player2, "Aven Envoy");
    }

    @Test
    void hasteAllowsActivationOnTurnAlarmistEnters() {
        harness.addToBattlefield(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new AvenEnvoy());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        assertThat(findPermanent(player1, "Skirk Alarmist").isTapped()).isTrue();
    }

    @Test
    void turningMorphCreatureFaceUpTriggersItsAbilityWithoutPayingMorphCost() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new Skinthinner());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent victim = addCreatureReady(player2, new AvenEnvoy());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(target.isFaceDown()).isFalse();
        harness.assertInGraveyard(player2, "Aven Envoy");
        harness.assertOnBattlefield(player1, "Skinthinner");

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Skinthinner");
    }

    @Test
    void activationDuringEndStepWaitsUntilFollowingEndStepToSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        addCreatureReady(player1, new SkirkAlarmist());
        Permanent target = addCreatureReady(player1, new AvenEnvoy());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Aven Envoy");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Aven Envoy");
    }
}
