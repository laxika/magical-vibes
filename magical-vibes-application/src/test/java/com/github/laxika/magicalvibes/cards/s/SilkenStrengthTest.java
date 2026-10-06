package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirResponseUnit;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilkenStrength.class, AirResponseUnit.class, GrizzlyBears.class, FountainOfYouth.class})
class SilkenStrengthTest extends BaseCardTest {

    @Test
    void entersAttachedUntapsAndEnhancesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        castSilkenStrength(creature);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void canEnchantVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        castSilkenStrength(vehicle);

        Permanent aura = findPermanent(player1, "Silken Strength");
        assertThat(aura.getAttachedTo()).isEqualTo(vehicle.getId());
    }

    @Test
    void cannotEnchantUnrelatedPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SilkenStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    private void castSilkenStrength(Permanent target) {
        harness.setHand(player1, List.of(new SilkenStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void untapWaitsForTriggeredAbilityToResolve() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player1, List.of(new SilkenStrength()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void canFlashOntoOpponentsCreatureDuringTheirUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        castSilkenStrength(creature);

        assertThat(findPermanent(player1, "Silken Strength").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    void untapsNoncreatureVehicleAndBoostsItWhenCrewed() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AirResponseUnit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        vehicle.tap();
        castSilkenStrength(vehicle);

        assertThat(vehicle.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.REACH)).isTrue();
    }
}
