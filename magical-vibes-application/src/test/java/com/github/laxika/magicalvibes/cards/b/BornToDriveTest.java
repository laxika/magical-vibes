package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.i.ImperialRecoveryUnit;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BornToDrive.class, EcologistsTerrarium.class, ImperialRecoveryUnit.class, Forest.class,
        GoldenTailDisciple.class, TrainedArynx.class})
class BornToDriveTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an enchanted creature by the number of creatures and Vehicles you control")
    void boostsEnchantedCreatureByControlledCreaturesAndVehicles() {
        Permanent enchantedCreature = addCreatureReady(player1, new GoldenTailDisciple());
        addCreatureReady(player1, new GoldenTailDisciple());
        harness.addToBattlefield(player1, new ImperialRecoveryUnit());
        addCreatureReady(player2, new GoldenTailDisciple());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BornToDrive());
        aura.setAttachedTo(enchantedCreature.getId());

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not boost the enchanted Vehicle while it is not a creature")
    void doesNotBoostNonCreatureEnchantedPermanent() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ImperialRecoveryUnit());
        int powerBeforeAura = gqs.getEffectivePower(gd, vehicle);
        int toughnessBeforeAura = gqs.getEffectiveToughness(gd, vehicle);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BornToDrive());
        aura.setAttachedTo(vehicle.getId());

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(powerBeforeAura);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(toughnessBeforeAura);
    }

    @Test
    @DisplayName("Channel creates two enhanced Pilot tokens and discards Born to Drive")
    void channelCreatesPilots() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ImperialRecoveryUnit());
        vehicle.setSummoningSick(false);
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Born to Drive");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.passBothPriorities();

        List<Permanent> pilots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .toList();
        assertThat(pilots).hasSize(2);
        assertThat(pilots).allSatisfy(pilot -> assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1));
        assertThat(pilots).allSatisfy(pilot -> assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1));
        harness.assertInGraveyard(player1, "Born to Drive");

        pilots.getFirst().setSummoningSick(false);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.handlePermanentChosen(player1, pilots.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilots.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a permanent that is neither an artifact nor a creature")
    void cannotEnchantUnrelatedPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEnchantOpponentsCreatureAndCountsAuraControllersPermanents() {
        Permanent creature = addCreatureReady(player2, new GoldenTailDisciple());
        addCreatureReady(player2, new GoldenTailDisciple());
        harness.addToBattlefield(player1, new ImperialRecoveryUnit());
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        addCreatureReady(player1, new GoldenTailDisciple());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void canEnchantNonVehicleArtifactWithoutBoostingItOrCountingIt() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EcologistsTerrarium());
        Permanent creature = addCreatureReady(player1, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new BornToDrive(), new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> artifact.getId().equals(permanent.getAttachedTo()));

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void channelCannotBeActivatedWithoutWhiteMana() {
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enchantedVehicleGainsBoostWhenCrewedAndIsCountedOnlyOnce() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ImperialRecoveryUnit());
        Permanent crew = addCreatureReady(player1, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, vehicle.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(6);
    }

    @Test
    void pilotsDoNotReceiveTheirCrewPowerBonusWhenSaddling() {
        harness.addToBattlefield(player1, new TrainedArynx());
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        List<Permanent> pilots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(pilots).hasSize(2);
        pilots.getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
    }
}
