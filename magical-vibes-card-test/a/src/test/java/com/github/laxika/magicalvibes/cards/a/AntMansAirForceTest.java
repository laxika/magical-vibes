package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AntMansAirForce.class, Forest.class, GrizzlyBears.class})
class AntMansAirForceTest extends BaseCardTest {

    @Test
    void attackingGivesTargetCreatureMinusOnePower() {
        addCreatureReady(player1, new AntMansAirForce());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void mayChooseNoTarget() {
        addCreatureReady(player1, new AntMansAirForce());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void targetMustBeACreature() {
        addCreatureReady(player1, new AntMansAirForce());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void modifierWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new AntMansAirForce());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void attackingAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new AntMansAirForce());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    void canTargetItself() {
        Permanent attacker = addCreatureReady(player1, new AntMansAirForce());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isEqualTo(-1);
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    @Test
    void canTargetAnotherCreatureYouControl() {
        Permanent attacker = addCreatureReady(player1, new AntMansAirForce());
        Permanent target = addCreatureReady(player1, new AntMansAirForce());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
    }

    @Test
    void triggerStillResolvesAfterSourceLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new AntMansAirForce());
        Permanent target = addCreatureReady(player2, new AntMansAirForce());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void returningTargetIsANewObjectAndIsNotAffected() {
        Permanent attacker = addCreatureReady(player1, new AntMansAirForce());
        AntMansAirForce targetCard = new AntMansAirForce();
        Permanent originalTarget = addCreatureReady(player2, targetCard);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, originalTarget.getId());
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(originalTarget);
        Permanent returnedTarget = harness.addToBattlefieldAndReturn(player2, targetCard);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(returnedTarget.getPowerModifier()).isZero();
        assertThat(returnedTarget.getToughnessModifier()).isZero();
        assertThat(attacker.getPowerModifier()).isZero();
    }
}
