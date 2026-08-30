package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoltenRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode deals 5 damage to a creature")
    void damageModeDealsFiveDamageToCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        cast(new int[]{0}, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Damage mode deals 5 damage to a planeswalker")
    void damageModeDealsFiveDamageToPlaneswalker() {
        Permanent planeswalker = new Permanent(new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        cast(new int[]{0}, List.of(planeswalker.getId()));

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipment mode destroys the target Equipment")
    void equipmentModeDestroysEquipment() {
        Permanent equipment = addEquipment(player2);
        cast(new int[]{1}, List.of(equipment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(equipment);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(equipment.getCard());
    }

    @Test
    @DisplayName("Both modes resolve with separate targets")
    void bothModesResolve() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = addEquipment(player2);
        cast(new int[]{0, 1}, List.of(creature.getId(), equipment.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature, equipment);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard(), equipment.getCard());
    }

    @Test
    @DisplayName("Each mode rejects the other mode's target")
    void modesRejectIllegalTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = addEquipment(player2);

        harness.setHand(player1, List.of(new MoltenRebuke()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(equipment.getId()), null))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new MoltenRebuke()));
        addMana();
        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new MoltenRebuke()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent addEquipment(com.github.laxika.magicalvibes.model.Player player) {
        Card equipmentCard = new Card();
        equipmentCard.setName("Test Equipment");
        equipmentCard.setType(CardType.ARTIFACT);
        equipmentCard.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        Permanent equipment = new Permanent(equipmentCard);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }
}
