package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlagwurmArmor.class, YotianSoldier.class})
class SlagwurmArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip {3} attaches Slagwurm Armor to target creature you control")
    void resolvingEquipAttachesToCreature() {
        Permanent armor = addSlagwurmArmorReady(player1);
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +0/+6")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent armor = addSlagwurmArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);
    }

    @Test
    @DisplayName("Equipped creature loses the boost when Slagwurm Armor leaves the battlefield")
    void creatureLosesBoostWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent armor = addSlagwurmArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(10);

        gd.playerBattlefields.get(player1.getId()).remove(armor);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Slagwurm Armor does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new YotianSoldier());
        Permanent otherCreature = addCreatureReady(player1, new YotianSoldier());
        Permanent armor = addSlagwurmArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Slagwurm Armor can be re-equipped to another creature")
    void canReEquipToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1, new YotianSoldier());
        Permanent secondCreature = addCreatureReady(player1, new YotianSoldier());
        Permanent armor = addSlagwurmArmorReady(player1);
        armor.setAttachedTo(firstCreature.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 2, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(10);
    }

    @Test
    @DisplayName("Slagwurm Armor cannot equip an opponent's creature")
    void cannotEquipOpponentsCreature() {
        addSlagwurmArmorReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new YotianSoldier());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    private Permanent addSlagwurmArmorReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SlagwurmArmor());
        perm.setSummoningSick(false);
        return perm;
    }
}
