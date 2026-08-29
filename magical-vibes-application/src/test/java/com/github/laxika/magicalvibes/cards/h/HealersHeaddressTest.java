package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealersHeaddressTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +0/+2")
    void equippedCreatureGetsToughnessBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent headdress = addHeaddressReady(player1);
        headdress.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The white ability attaches Healer's Headdress to a creature you control")
    void whiteAbilityAttachesToControlledCreature() {
        Permanent headdress = addHeaddressReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, indexOf(player1, headdress), 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(headdress.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The equip ability attaches Healer's Headdress to a creature you control")
    void equipAbilityAttachesToControlledCreature() {
        Permanent headdress = addHeaddressReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, headdress), 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(headdress.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The equipped creature can prevent the next damage to a player")
    void equippedCreaturePreventsNextDamageToPlayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent headdress = addHeaddressReady(player1);
        headdress.setAttachedTo(creature.getId());
        addCreatureReady(player2, new HillGiant()).setAttacking(true);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, indexOf(player1, creature), null, player1.getId());
        harness.passBothPriorities();

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    private Permanent addHeaddressReady(Player player) {
        return addCreatureReady(player, new HealersHeaddress());
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
