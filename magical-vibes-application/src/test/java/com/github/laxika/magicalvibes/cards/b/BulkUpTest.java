package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
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

@CardUsed({BulkUp.class, GrizzlyBears.class, FountainOfYouth.class, GiantGrowth.class})
class BulkUpTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles target creature's power without changing its toughness")
    void doublesTargetPower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = bear.getId();
        harness.castAndResolveInstant(player1, 0, bearId);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Power doubling wears off at cleanup")
    void powerDoublingWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearId = bear.getId();
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Flashback doubles target creature's power and exiles Bulk Up")
    void flashbackDoublesPowerAndExilesCard() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID bearId = bear.getId();
        harness.castAndResolveFlashback(player1, 0, bearId);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Bulk Up");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bulk Up"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Doubles negative power rather than treating it as zero")
    void doublesNegativePower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setPowerModifier(-3);
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubling zero power leaves it at zero")
    void doublesZeroPower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setPowerModifier(-2);
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can double an opponent's creature's power")
    void doublesOpponentsCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Bulk Up");
    }

    @Test
    @DisplayName("Uses power at resolution and fixes the resulting bonus")
    void doublesPowerAtResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BulkUp(), new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, bear.getId());
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(8);
    }

    @Test
    @DisplayName("Normal cast followed by flashback doubles power twice")
    void flashbackDoublesAlreadyDoubledPower() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BulkUp()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.assertInGraveyard(player1, "Bulk Up");
        harness.castAndResolveFlashback(player1, 0, bear.getId());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Bulk Up");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bulk Up"));
    }
}
