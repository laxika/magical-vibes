package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GuidelightOptimizer;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({LightshieldParry.class, GuidelightOptimizer.class, Plains.class})
class LightshieldParryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Lightshield Parry gives target creature +2/+2 until end of turn")
    void boostsTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Lightshield Parry's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Lightshield Parry cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player1, "Plains");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cycling Lightshield Parry discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.setLibrary(player1, List.of(new GuidelightOptimizer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightshield Parry");
        harness.assertInHand(player1, "Guidelight Optimizer");
    }

    @Test
    @DisplayName("Lightshield Parry can boost an opponent's creature")
    void boostsOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Lightshield Parry");
    }

    @Test
    @DisplayName("Lightshield Parry does not boost another creature when its target leaves")
    void doesNotBoostAnotherCreatureWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GuidelightOptimizer());
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.setGraveyard(player1, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightshield Parry");
    }

    @Test
    @DisplayName("Cycling pays its discard cost before the draw resolves and accepts colored mana")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.setLibrary(player1, List.of(new GuidelightOptimizer()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Lightshield Parry");
        harness.assertNotInHand(player1, "Lightshield Parry");
        harness.assertNotInHand(player1, "Guidelight Optimizer");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Guidelight Optimizer");
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana and does not discard the card")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new LightshieldParry()));
        harness.setLibrary(player1, List.of(new GuidelightOptimizer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lightshield Parry");
        harness.assertNotInGraveyard(player1, "Lightshield Parry");
        harness.assertNotInHand(player1, "Guidelight Optimizer");
        assertThat(gd.stack).isEmpty();
    }
}
