package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AethersphereHarvester;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SiegeModification.class, SleekSchooner.class, GrizzlyBears.class,
        FountainOfYouth.class, AethersphereHarvester.class})
class SiegeModificationTest extends BaseCardTest {

    @Test
    @DisplayName("Siege Modification makes a Vehicle a creature and gives it +3/+0 and first strike")
    void modifiesVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new SleekSchooner());

        castSiegeModification(vehicle);

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Siege Modification gives an enchanted creature +3/+0 and first strike")
    void modifiesCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castSiegeModification(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Siege Modification cannot target a noncreature non-Vehicle permanent")
    void cannotTargetOtherPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SiegeModification()));
        addSiegeModificationMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Vehicle");
    }

    @Test
    @DisplayName("Siege Modification can animate an opponent's Vehicle without affecting other Vehicles")
    void modifiesOpponentsVehicleOnly() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new AethersphereHarvester());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new AethersphereHarvester());

        castSiegeModification(enchanted);

        assertThat(gqs.isCreature(gd, enchanted)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.FLYING)).isTrue();
        assertThat(gqs.isCreature(gd, other)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Removing Siege Modification ends its animation, power bonus, and first strike")
    void effectsEndWhenAuraLeaves() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new AethersphereHarvester());
        castSiegeModification(vehicle);
        Permanent aura = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Siege Modification"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        harness.assertInGraveyard(player1, "Siege Modification");
        harness.assertOnBattlefield(player1, "Aethersphere Harvester");
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }

    private void castSiegeModification(Permanent target) {
        harness.setHand(player1, List.of(new SiegeModification()));
        addSiegeModificationMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addSiegeModificationMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
