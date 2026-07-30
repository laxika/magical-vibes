package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KiteShieldTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip {3} attaches Kite Shield to target creature you control")
    void resolvingEquipAttachesToCreature() {
        Permanent shield = addKiteShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +0/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addKiteShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equipped creature loses the boost when Kite Shield leaves the battlefield")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addKiteShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(shield);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Kite Shield does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addKiteShieldReady(player1);
        shield.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
    }

    private Permanent addKiteShieldReady(Player player) {
        Permanent perm = new Permanent(new KiteShield());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
