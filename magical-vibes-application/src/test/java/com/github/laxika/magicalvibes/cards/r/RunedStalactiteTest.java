package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class RunedStalactiteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Runed Stalactite to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent stalactite = addStalactiteReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(stalactite.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent stalactite = addStalactiteReady(player1);
        stalactite.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Equipped creature becomes every creature type (gets Soldier lord boost)")
    void equippedCreatureIsEveryCreatureType() {
        // Grizzly Bears is a Bear, not a Soldier; Field Marshal boosts other Soldiers.
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent bears = addReadyCreature(player1);
        Permanent stalactite = addStalactiteReady(player1);
        stalactite.setAttachedTo(bears.getId());

        // 2/2 base + 1/1 (Stalactite) + 1/1 (Field Marshal, now that Bears is a Soldier)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and creature types when Stalactite is removed")
    void creatureLosesBonusesWhenEquipmentRemoved() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent bears = addReadyCreature(player1);
        Permanent stalactite = addStalactiteReady(player1);
        stalactite.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(stalactite);

        // Back to base 2/2, no longer a Soldier
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Stalactite does not affect unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addReadyCreature(player1);
        Permanent other = addReadyCreature(player1);
        Permanent stalactite = addStalactiteReady(player1);
        stalactite.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    private Permanent addStalactiteReady(Player player) {
        Permanent perm = new Permanent(new RunedStalactite());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
