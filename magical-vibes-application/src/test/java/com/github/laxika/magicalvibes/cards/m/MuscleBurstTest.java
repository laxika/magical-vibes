package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkwaterEgg;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MuscleBurst.class, DarkwaterEgg.class, WoodlandDruid.class})
class MuscleBurstTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Muscle Burst gives target creature +3/+3 with no matching graveyard cards")
    void resolvesWithBaseBoost() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts Muscle Burst cards in all graveyards")
    void countsMatchingCardsInAllGraveyards() {
        harness.setGraveyard(player1, List.of(new MuscleBurst()));
        harness.setGraveyard(player2, List.of(new MuscleBurst()));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(5);
        assertThat(bear.getToughnessModifier()).isEqualTo(5);
    }

    @Test
    @DisplayName("The resolving Muscle Burst does not count itself")
    void resolvingCopyDoesNotCountItself() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.setHand(player1, List.of(new MuscleBurst(), new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        assertThat(bear.getPowerModifier()).isEqualTo(3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(7);
        assertThat(bear.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Muscle Burst's boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not count other cards in all graveyards")
    void ignoresOtherCardsInGraveyards() {
        harness.setGraveyard(player1, List.of(new WoodlandDruid()));
        harness.setGraveyard(player2, List.of(new WoodlandDruid()));
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WoodlandDruid());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(3);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Muscle Burst")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new WoodlandDruid());
        harness.addToBattlefield(player1, new DarkwaterEgg());
        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        var targetId = harness.getPermanentId(player1, "Darkwater Egg");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
