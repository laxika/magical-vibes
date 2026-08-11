package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColossusHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Colossus Hammer to the target creature")
    void resolvingEquipAttaches() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +10/+10 and loses flying")
    void equippedCreatureGetsBoostAndLosesFlying() {
        Permanent creature = addReadyFlyingCreature(player1);
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(14);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Removing Colossus Hammer removes its effects from the creature")
    void removingHammerRemovesItsEffects() {
        Permanent creature = addReadyFlyingCreature(player1);
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(hammer);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Re-equipping Colossus Hammer transfers its effects")
    void reEquipTransfersEffects() {
        Permanent hammer = addHammerReady(player1);
        Permanent flyingCreature = addReadyFlyingCreature(player1);
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        hammer.setAttachedTo(flyingCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, 0, null, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, flyingCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(12);
    }

    private Permanent addHammerReady(Player player) {
        Permanent perm = new Permanent(new ColossusHammer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyFlyingCreature(Player player) {
        Permanent perm = new Permanent(new AzureDrake());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
