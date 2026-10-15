package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenSeaClan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.SkySkiff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeardedAxe.class, DwarvenSeaClan.class, GrizzlyBears.class,
        LeoninScimitar.class, SkySkiff.class})
class BeardedAxeTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostForEachControlledDwarfEquipmentAndVehicle() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new BeardedAxe());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenSeaClan());
        harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefieldAndReturn(player1, new SkySkiff());

        int powerBefore = gqs.getEffectivePower(gd, dwarf);
        int toughnessBefore = gqs.getEffectiveToughness(gd, dwarf);
        axe.setAttachedTo(dwarf.getId());

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(powerBefore + 4);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(toughnessBefore + 4);
    }

    @Test
    void equipTwoAttachesAxeToCreatureYouControl() {
        Permanent axe = harness.addToBattlefieldAndReturn(player1, new BeardedAxe());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(axe.getAttachedTo()).isEqualTo(creature.getId());
    }
}
