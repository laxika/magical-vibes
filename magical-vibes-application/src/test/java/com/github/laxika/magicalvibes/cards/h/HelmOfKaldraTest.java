package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KiteShield;
import com.github.laxika.magicalvibes.cards.s.ShieldOfKaldra;
import com.github.laxika.magicalvibes.cards.s.SwordOfKaldra;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfKaldraTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike, trample, and haste")
    void equippedCreatureHasKeywords() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        helm.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The ability does nothing unless all three Kaldra Equipment are controlled")
    void abilityRequiresAllKaldraEquipment() {
        addHelmReady(player1);
        addEquipmentReady(player1, new SwordOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Kaldra")).isEmpty();
    }

    @Test
    @DisplayName("Creates legendary Kaldra and attaches only the three Kaldra Equipment")
    void createsAndEquipsKaldra() {
        Permanent helm = addHelmReady(player1);
        Permanent sword = addEquipmentReady(player1, new SwordOfKaldra());
        Permanent shield = addEquipmentReady(player1, new ShieldOfKaldra());
        Permanent unrelatedEquipment = addEquipmentReady(player1, new KiteShield());
        Permanent opponentSword = addEquipmentReady(player2, new SwordOfKaldra());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var kaldraPermanents = findPermanents(player1, "Kaldra");
        assertThat(kaldraPermanents).hasSize(1);
        Permanent kaldra = kaldraPermanents.getFirst();
        assertThat(kaldra.getCard().getPower()).isEqualTo(4);
        assertThat(kaldra.getCard().getToughness()).isEqualTo(4);
        assertThat(kaldra.getCard().getSubtypes()).containsExactly(CardSubtype.AVATAR);
        assertThat(kaldra.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(helm.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(sword.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(shield.getAttachedTo()).isEqualTo(kaldra.getId());
        assertThat(unrelatedEquipment.getAttachedTo()).isNull();
        assertThat(opponentSword.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip {2} attaches Helm of Kaldra to a creature you control")
    void equipAttachesToCreature() {
        Permanent helm = addHelmReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addHelmReady(Player player) {
        return addEquipmentReady(player, new HelmOfKaldra());
    }

    private Permanent addEquipmentReady(Player player, Card card) {
        Permanent equipment = new Permanent(card);
        equipment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }
}
