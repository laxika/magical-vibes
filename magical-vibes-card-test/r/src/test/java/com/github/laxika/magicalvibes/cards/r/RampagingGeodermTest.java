package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.InvasionOfErgamon;
import com.github.laxika.magicalvibes.cards.p.PortentTracker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RampagingGeoderm.class, InvasionOfErgamon.class, PortentTracker.class})
class RampagingGeodermTest extends BaseCardTest {

    @Test
    void givesTargetAttackingCreatureTemporaryBoostWhenAttackingPlayer() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(geoderm.getPowerModifier()).isEqualTo(1);
        assertThat(geoderm.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geoderm.getPowerModifier()).isZero();
        assertThat(geoderm.getToughnessModifier()).isZero();
    }

    @Test
    void putsCounterOnTargetAttackingCreatureWhenAttackingBattle() {
        Permanent battle = addBattle(player2);
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackersAt(battle);
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(geoderm.getPowerModifier()).isZero();
        assertThat(geoderm.getToughnessModifier()).isZero();
    }

    @Test
    void battleCounterRemainsAfterEndOfTurn() {
        Permanent battle = addBattle(player2);
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());

        declareAttackersAt(battle);
        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addBattle(Player controller) {
        Permanent battle = harness.addToBattlefieldAndReturn(controller, new InvasionOfErgamon());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        battle.setProtectorPlayerId(player2.getId());
        return battle;
    }

    @Test
    void triggersWhenAnotherCreatureAttacksAndGeodermDoesNot() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, tracker.getId());
        resolveAllTriggers();

        assertThat(tracker.getPowerModifier()).isEqualTo(1);
        assertThat(tracker.getToughnessModifier()).isEqualTo(1);
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(geoderm.getPowerModifier()).isZero();
    }

    @Test
    void triggersOnlyOnceWhenMultipleCreaturesAttack() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, tracker.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(tracker.getPowerModifier()).isEqualTo(1);
        assertThat(tracker.getToughnessModifier()).isEqualTo(1);
        assertThat(geoderm.getPowerModifier()).isZero();
    }

    @Test
    void doesNotTriggerForAnOpponentsAttack() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player2, new PortentTracker());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(tracker.getPowerModifier()).isZero();
        assertThat(geoderm.getPowerModifier()).isZero();
    }

    @Test
    void cannotTargetANonattackingCreature() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, tracker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, geoderm.getId());
        resolveAllTriggers();
        assertThat(geoderm.getPowerModifier()).isEqualTo(1);
        assertThat(tracker.getPowerModifier()).isZero();
    }

    @Test
    void doesNothingIfTargetStopsAttackingBeforeResolution() {
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player1, new PortentTracker());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, tracker.getId());
        tracker.setAttacking(false);
        tracker.setAttackTarget(null);
        resolveAllTriggers();

        assertThat(tracker.getPowerModifier()).isZero();
        assertThat(tracker.getToughnessModifier()).isZero();
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(geoderm.getPowerModifier()).isZero();
    }

    @Test
    void usesTargetsDefenderWhenCreaturesAttackDifferentDefenders() {
        Permanent battle = addBattle(player2);
        Permanent geoderm = addCreatureReady(player1, new RampagingGeoderm());
        Permanent tracker = addCreatureReady(player1, new PortentTracker());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1), Map.of(1, battle.getId()));
        harness.handlePermanentChosen(player1, tracker.getId());
        resolveAllTriggers();

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tracker.getPowerModifier()).isZero();
        assertThat(tracker.getToughnessModifier()).isZero();
        assertThat(geoderm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(geoderm.getPowerModifier()).isZero();
    }

    private void declareAttackersAt(Permanent battle) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
    }
}
