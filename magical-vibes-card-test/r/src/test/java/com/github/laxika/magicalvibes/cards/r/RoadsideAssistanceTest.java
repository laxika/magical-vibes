package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoadsideAssistance.class, DuskLegionDreadnought.class, GrizzlyBears.class, FountainOfYouth.class})
class RoadsideAssistanceTest extends BaseCardTest {

    @Test
    void enchantsVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new RoadsideAssistance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, vehicle.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void entersAttachedCreatesPilotAndAppliesBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new RoadsideAssistance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        Permanent aura = findPermanent(player1, "Roadside Assistance");
        Permanent pilot = findPermanent(player1, "Pilot");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(pilot.getCard().getSubtypes()).contains(CardSubtype.PILOT);
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);

        pilot.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.handlePermanentChosen(player1, pilot.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    void enchantedCreatureGetsBoostAndLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RoadsideAssistance());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotEnchantUnrelatedPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RoadsideAssistance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    void enchantedVehicleGetsBonusWhenCrewedBySummoningSickPilot() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new RoadsideAssistance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, vehicle.getId());
        resolveAllTriggers();

        Permanent aura = findPermanent(player1, "Roadside Assistance");
        Permanent pilot = findPermanent(player1, "Pilot");
        assertThat(aura.getAttachedTo()).isEqualTo(vehicle.getId());
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.LIFELINK)).isTrue();
        assertThat(pilot.isSummoningSick()).isTrue();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(7);
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    void enchantingOpponentsCreatureCreatesPilotForAuraController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RoadsideAssistance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Roadside Assistance").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertOnBattlefield(player1, "Pilot");
        harness.assertNotOnBattlefield(player2, "Pilot");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }
}
