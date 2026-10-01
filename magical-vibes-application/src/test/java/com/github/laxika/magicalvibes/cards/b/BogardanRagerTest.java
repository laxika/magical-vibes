package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogardanRager.class, AshcoatBear.class, Forest.class})
class BogardanRagerTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        harness.castFromHand(player1, new BogardanRager(), "{5}{R}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("ETB gives a target creature +4/+0 until end of turn")
    void etbBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        castBogardanRager(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature its controller controls")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        castBogardanRager(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        castBogardanRager(target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if the target creature leaves before resolution")
    void etbFizzlesIfTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        castBogardanRager(target.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BogardanRager()));
        addManaForBogardanRager();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Does not create an ETB trigger when no creature can be targeted")
    void doesNotTriggerWithoutLegalTarget() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new BogardanRager(), "{5}{R}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bogardan Rager");
        assertThat(gd.stack).isEmpty();
    }

    private void castBogardanRager(UUID targetId) {
        harness.setHand(player1, List.of(new BogardanRager()));
        addManaForBogardanRager();
        harness.castCreature(player1, 0, List.of(targetId));
    }

    private void addManaForBogardanRager() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
