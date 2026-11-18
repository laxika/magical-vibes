package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
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
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;

@CardUsed({Enrage.class, Forest.class, GoblinBrigand.class, GrizzlyBears.class})
class EnrageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives target creature +X/+0")
    void resolvesAndBoostsPowerOnly() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Enrages retain their own X values and their boosts add together")
    void multipleEnragesUseIndependentXValues() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage(), new Enrage()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castInstant(player1, 0, 2, bear.getId());
        harness.castInstant(player1, 0, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Enrage");
        assertThat(gd.stack).isEmpty();
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

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature controlled by an opponent")
    void canTargetOpponentCreature() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, 2, opponentBear.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, opponentBear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, opponentBear)).isZero();
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can cast with X equal to zero")
    void canCastWithZeroX() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GoblinBrigand()); // legal creature target so the spell is castable (CR 601.2c)
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, forest.getId()))
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

        assertThat(PermanentGrants.powerBoostOf(gd, goblin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, goblin)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("X can be zero")
    void resolvesWithZeroBoost() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new Enrage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, goblin.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, goblin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, goblin)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, goblin)).isEqualTo(2);
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
