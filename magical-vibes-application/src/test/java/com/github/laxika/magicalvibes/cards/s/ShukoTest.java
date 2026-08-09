package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShukoTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shuko = addShukoReady(player1);
        shuko.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving equip attaches Shuko to target creature without mana")
    void resolvingEquipAttachesWithoutMana() {
        Permanent shuko = addShukoReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuko.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Shuko does not affect an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent shuko = addShukoReady(player1);
        shuko.setAttachedTo(equippedCreature.getId());

        Permanent unequippedCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .filter(permanent -> !permanent.getId().equals(equippedCreature.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, unequippedCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unequippedCreature)).isEqualTo(2);
    }

    private Permanent addShukoReady(Player player) {
        Permanent permanent = new Permanent(new Shuko());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
