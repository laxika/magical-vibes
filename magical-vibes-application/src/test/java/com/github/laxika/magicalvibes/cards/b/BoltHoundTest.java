package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoltHound.class, GrizzlyBears.class})
class BoltHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+0 when Bolt Hound attacks")
    void otherCreaturesGetBoost() {
        addCreatureReady(player1, new BoltHound());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Bolt Hound does not boost itself")
    void doesNotBoostItself() {
        Permanent hound = addCreatureReady(player1, new BoltHound());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(hound.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures are not boosted")
    void opponentCreaturesNotBoosted() {
        addCreatureReady(player1, new BoltHound());
        Permanent enemy = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(enemy.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new BoltHound());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("A nonattacking Bolt Hound is boosted by another Hound's attack")
    void boostsAnotherHound() {
        Permanent attacker = addCreatureReady(player1, new BoltHound());
        Permanent otherHound = addCreatureReady(player1, new BoltHound());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(otherHound.getPowerModifier()).isEqualTo(1);
        assertThat(otherHound.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Two attacking Hounds boost each other and their boosts stack on other creatures")
    void multipleAttackTriggersStack() {
        Permanent first = addCreatureReady(player1, new BoltHound());
        Permanent second = addCreatureReady(player1, new BoltHound());
        Permanent third = addCreatureReady(player1, new BoltHound());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(third.getPowerModifier()).isEqualTo(2);
        assertThat(third.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures entering before the attack trigger resolves receive the boost")
    void creaturesEnteringBeforeResolutionAreBoosted() {
        addCreatureReady(player1, new BoltHound());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new BoltHound());
        resolveAllTriggers();

        assertThat(newcomer.getPowerModifier()).isEqualTo(1);
        assertThat(newcomer.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures entering after the attack trigger resolves do not receive the boost")
    void creaturesEnteringAfterResolutionAreNotBoosted() {
        addCreatureReady(player1, new BoltHound());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new BoltHound());

        assertThat(newcomer.getPowerModifier()).isZero();
        assertThat(newcomer.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A freshly cast Bolt Hound can attack and boost other creatures immediately")
    void freshlyCastHoundCanAttack() {
        Permanent other = addCreatureReady(player1, new BoltHound());
        harness.castFromHand(player1, new BoltHound(), "{2}{R}");
        resolveAllTriggers();
        Permanent newcomer = gd.playerBattlefields.get(player1.getId()).get(1);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(other.getPowerModifier()).isEqualTo(1);
        assertThat(newcomer.getPowerModifier()).isZero();
    }
}
