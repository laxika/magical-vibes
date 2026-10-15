package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainAmericasMotorcycle.class, DuskLegionDreadnought.class,
        FountainOfYouth.class, GrizzlyBears.class})
class CaptainAmericasMotorcycleTest extends BaseCardTest {

    @Test
    void entersAndBoostsTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMotorcycle(bears);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void entersAndBoostsTargetVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        castMotorcycle(vehicle);

        assertThat(vehicle.getPowerModifier()).isEqualTo(2);
        assertThat(vehicle.getToughnessModifier()).isZero();
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castMotorcycle(bears);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    void cannotTargetNonCreatureNonVehicle() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CaptainAmericasMotorcycle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void crewsWithOnePower() {
        Permanent motorcycle = harness.addToBattlefieldAndReturn(player1,
                new CaptainAmericasMotorcycle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        motorcycle.setSummoningSick(false);
        bears.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, motorcycle)).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    private void castMotorcycle(Permanent target) {
        harness.setHand(player1, List.of(new CaptainAmericasMotorcycle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void canFlashInDuringOpponentsCombatAndBoostTheirCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.ensurePriority(player1);

        castMotorcycle(bears);

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Captain America's Motorcycle");
    }

    @Test
    void canTargetItselfWhenItEntersWithoutBeingCrewed() {
        harness.setHand(player1, List.of(new CaptainAmericasMotorcycle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent motorcycle = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, motorcycle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, motorcycle)).isFalse();
        assertThat(motorcycle.getPowerModifier()).isEqualTo(2);
        assertThat(motorcycle.getToughnessModifier()).isZero();
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationExpires() {
        Permanent motorcycle = harness.addToBattlefieldAndReturn(player1,
                new CaptainAmericasMotorcycle());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, motorcycle)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(motorcycle.isTapped()).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, motorcycle)).isFalse();
    }
}
