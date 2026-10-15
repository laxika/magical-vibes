package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
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

@CardUsed({DeathWind.class, GrizzlyBears.class, FountainOfYouth.class, AngelicWall.class,
        Cloudshift.class, MoorlandInquisitor.class})
class DeathWindTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving gives the target creature -X/-X")
    void shrinksTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(-1);
        assertThat(bear.getToughnessModifier()).isEqualTo(-1);
        assertThat(bear.getEffectivePower()).isEqualTo(1);
        assertThat(bear.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enough -X/-X kills the creature")
    void lethalShrinkDestroysCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, 2, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("X=0 leaves the creature unchanged")
    void xZeroDoesNothing() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The penalty wears off at end of turn")
    void penaltyWearsOff() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can shrink a creature controlled by the caster")
    void shrinksOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Power can become negative while the creature survives")
    void negativePowerDoesNotKillCreature() {
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 3, wall.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Angelic Wall");
        assertThat(gqs.getEffectivePower(gd, wall)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(1);
    }

    @Test
    @DisplayName("X greater than toughness puts the creature into the graveyard")
    void negativeToughnessKillsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, 3, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Moorland Inquisitor");
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("A creature that leaves and returns is no longer the spell's target")
    void flickeredTargetIsNotShrunk() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.setHand(player2, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 2, original.getId());
        harness.castAndResolveInstant(player2, 0, original.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Moorland Inquisitor");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Death Wind");
        assertThat(gd.stack).isEmpty();
    }
}
