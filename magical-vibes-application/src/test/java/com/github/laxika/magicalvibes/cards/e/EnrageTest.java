package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DingusEgg;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enrage.class, GrizzlyBears.class, DingusEgg.class, GoblinBrigand.class, Stabilizer.class})
class EnrageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature +X/+0")
    void resolvesAndBoostsPowerOnly() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, opponentBear.getId());
        harness.passBothPriorities();

        assertThat(opponentBear.getPowerModifier()).isEqualTo(2);
        assertThat(opponentBear.getToughnessModifier()).isZero();
        assertThat(opponentBear.getEffectivePower()).isEqualTo(4);
        assertThat(opponentBear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can cast with X equal to zero")
    void canCastWithZeroX() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent dingusEgg = harness.addToBattlefieldAndReturn(player1, new DingusEgg());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, dingusEgg.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinBrigand());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 1, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(1);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getEffectivePower()).isEqualTo(3);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("X can be zero")
    void resolvesWithZeroBoost() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(goblin.getPowerModifier()).isEqualTo(0);
        assertThat(goblin.getToughnessModifier()).isEqualTo(0);
        assertThat(goblin.getEffectivePower()).isEqualTo(2);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, goblin.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Enrage");
    }
}
