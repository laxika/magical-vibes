package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bravado.class, BullHippo.class, WornPowerstone.class})
class BravadoTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each other creature you control")
    void boostsPerOtherCreatureYouControl() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        harness.addToBattlefield(player1, new WornPowerstone());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Bravado());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts creatures controlled by the Aura controller on an opponent's creature")
    void countsAuraControllersCreatures() {
        Permanent opponentHost = harness.addToBattlefieldAndReturn(player2, new BullHippo());
        Permanent ownA = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        Permanent ownB = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        Permanent opponentOther = harness.addToBattlefieldAndReturn(player2, new BullHippo());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Bravado());
        aura.setAttachedTo(opponentHost.getId());

        assertThat(gqs.getEffectivePower(gd, opponentHost)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentHost)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentOther)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownA)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownB)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost changes as other creatures enter and leave")
    void updatesDynamically() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BullHippo());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Bravado());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);

        Permanent other = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost ends when Bravado leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        harness.addToBattlefield(player1, new BullHippo());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Bravado());
        aura.setAttachedTo(host.getId());

        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Bravado attaches it to the target creature and applies the boost")
    void castingAttachesAndAppliesBoost() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new BullHippo());
        harness.addToBattlefield(player1, new BullHippo());
        harness.setHand(player1, List.of(new Bravado()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Bravado");
        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new Bravado()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent artifact = findPermanent(player1, "Worn Powerstone");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
