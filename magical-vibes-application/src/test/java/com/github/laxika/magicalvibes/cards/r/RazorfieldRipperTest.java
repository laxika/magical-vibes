package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RazorfieldRipper.class, GrizzlyBears.class})
class RazorfieldRipperTest extends BaseCardTest {

    @Test
    void attackAddsEnergyThenBoostsByCurrentEnergy() {
        Permanent ripper = addReadyRipper();
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ripper)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ripper)).isEqualTo(6);
    }

    @Test
    void equippedCreatureAttackingAddsEnergyAndGetsTheBoost() {
        Permanent ripper = addReadyRipper();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        ripper.setAttachedTo(creature.getId());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    void reconfigureCanUseEnergyAndUnattach() {
        Permanent ripper = addReadyRipper();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ripper.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.isCreature(gd, ripper)).isFalse();

        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(ripper.getAttachedTo()).isNull();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gqs.isCreature(gd, ripper)).isTrue();
    }

    @Test
    void reconfigureCanUseMana() {
        Permanent ripper = addReadyRipper();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ripper.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReadyRipper() {
        Permanent ripper = new Permanent(new RazorfieldRipper());
        ripper.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ripper);
        return ripper;
    }
}
