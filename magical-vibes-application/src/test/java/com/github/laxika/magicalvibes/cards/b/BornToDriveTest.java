package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BornToDrive.class, DuskLegionDreadnought.class, Forest.class, GrizzlyBears.class})
class BornToDriveTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an enchanted creature by the number of creatures and Vehicles you control")
    void boostsEnchantedCreatureByControlledCreaturesAndVehicles() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = new Permanent(new BornToDrive());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not boost the enchanted Vehicle while it is not a creature")
    void doesNotBoostNonCreatureEnchantedPermanent() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        int powerBeforeAura = gqs.getEffectivePower(gd, vehicle);
        int toughnessBeforeAura = gqs.getEffectiveToughness(gd, vehicle);
        Permanent aura = new Permanent(new BornToDrive());
        aura.setAttachedTo(vehicle.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(powerBeforeAura);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(toughnessBeforeAura);
    }

    @Test
    @DisplayName("Channel creates two enhanced Pilot tokens and discards Born to Drive")
    void channelCreatesPilots() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Permanent> pilots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .toList();
        assertThat(pilots).hasSize(2);
        assertThat(pilots).allSatisfy(pilot -> assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1));
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
    @DisplayName("Cannot enchant a permanent that is neither a creature nor a Vehicle")
    void cannotEnchantUnrelatedPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BornToDrive()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
