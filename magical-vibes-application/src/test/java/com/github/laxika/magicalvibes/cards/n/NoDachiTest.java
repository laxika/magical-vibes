package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoDachiTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and first strike")
    void equippedCreatureBoostedAndHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent noDachi = addNoDachiReady(player1);
        noDachi.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} attaches No-Dachi to a creature you control")
    void equipAttachesToCreature() {
        Permanent noDachi = addNoDachiReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(noDachi.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip costs three mana")
    void equipCostsThreeMana() {
        addNoDachiReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Unattached No-Dachi boosts nothing")
    void unattachedBoostsNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addNoDachiReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Boost and first strike end when No-Dachi leaves the battlefield")
    void boostEndsWhenEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent noDachi = addNoDachiReady(player1);
        noDachi.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(noDachi);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addNoDachiReady(Player player) {
        Permanent perm = new Permanent(new NoDachi());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
