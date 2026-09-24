package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HelmOfKaldra;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.cards.n.NemesisMask;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShieldOfKaldra.class, SwordOfKaldra.class, HelmOfKaldra.class,
        NemesisMask.class, MyrMoonvessel.class})
class ShieldOfKaldraTest extends BaseCardTest {

    @Test
    @DisplayName("Shield of Kaldra and other Kaldra Equipment have indestructible")
    void kaldraEquipmentHasIndestructible() {
        Permanent shield = addShieldReady(player1);
        Permanent sword = addEquipmentReady(player2, new SwordOfKaldra());
        Permanent otherEquipment = addEquipmentReady(player2, new NemesisMask());

        assertThat(gqs.hasKeyword(gd, shield, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, sword, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherEquipment, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Helm of Kaldra has indestructible")
    void helmOfKaldraHasIndestructible() {
        addShieldReady(player1);
        Permanent helm = addEquipmentReady(player2, new HelmOfKaldra());

        assertThat(gqs.hasKeyword(gd, helm, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature has indestructible while Shield of Kaldra is attached")
    void equippedCreatureHasIndestructible() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();

        shield.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isTrue();

        shield.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Equip {4} attaches Shield of Kaldra to a creature you control")
    void equipAttachesToCreature() {
        Permanent shield = addShieldReady(player1);
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shield.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addShieldReady(Player player) {
        return addEquipmentReady(player, new ShieldOfKaldra());
    }

    private Permanent addEquipmentReady(Player player, Card card) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, card);
        equipment.setSummoningSick(false);
        return equipment;
    }
}
