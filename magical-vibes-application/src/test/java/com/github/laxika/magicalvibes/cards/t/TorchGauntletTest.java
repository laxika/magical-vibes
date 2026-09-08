package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TorchGauntletTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Torch Gauntlet to a creature")
    void equipsCreature() {
        Permanent gauntlet = addGauntletReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gauntlet = addGauntletReady(player1);
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Unattached Torch Gauntlet does not boost creatures")
    void unattachedGauntletDoesNotBoostCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addGauntletReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature loses Torch Gauntlet's boost when it is unattached")
    void creatureLosesBoostWhenGauntletIsUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent gauntlet = addGauntletReady(player1);
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gauntlet.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    private Permanent addGauntletReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TorchGauntlet());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
