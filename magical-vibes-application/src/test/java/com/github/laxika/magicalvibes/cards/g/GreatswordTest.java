package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreatswordTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Greatsword and gives the creature +3/+0")
    void equipAttachesAndBoosts() {
        addGreatswordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent greatsword = findPermanent(player1, "Greatsword");
        assertThat(greatsword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} costs three mana")
    void equipCostsThreeMana() {
        addGreatswordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot equip with only two mana")
    void cannotEquipWithTwoMana() {
        addGreatswordReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Creature loses the boost when Greatsword leaves the battlefield")
    void boostEndsWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent greatsword = addGreatswordReady(player1);
        greatsword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(greatsword);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Greatsword does not boost unequipped creatures")
    void doesNotBoostOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addGreatswordReady(player1).setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    private Permanent addGreatswordReady(Player player) {
        Permanent perm = new Permanent(new Greatsword());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
