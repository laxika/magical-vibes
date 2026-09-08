package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieryAnnihilationTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and exiles the target creature and attached Equipment")
    void exilesCreatureAndAttachedEquipment() {
        Permanent creature = addCreature(player2, new GrizzlyBears());
        Permanent equipment = addEquipment(creature);

        cast(List.of(creature.getId(), equipment.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gameData.exiledCards).extracting(exiled -> exiled.card().getName())
                .contains("Grizzly Bears", "Leonin Scimitar");
    }

    @Test
    @DisplayName("Can resolve without choosing an Equipment")
    void equipmentTargetIsOptional() {
        Permanent creature = addCreature(player2, new AvatarOfMight());

        cast(List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(5);
        assertThat(creature.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Rejects an Equipment that is not attached to the target creature")
    void equipmentMustBeAttachedToCreature() {
        Permanent creature = addCreature(player2, new AvatarOfMight());
        Permanent equipment = addEquipment(null);

        harness.setHand(player1, List.of(new FieryAnnihilation()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), equipment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attached");
    }

    @Test
    @DisplayName("Does not exile the Equipment if it becomes unattached before resolution")
    void unattachedEquipmentIsNotExiled() {
        Permanent creature = addCreature(player2, new AvatarOfMight());
        Permanent equipment = addEquipment(creature);

        cast(List.of(creature.getId(), equipment.getId()));
        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(creature.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not exile the Equipment if the creature is illegal on resolution")
    void illegalCreatureTargetLeavesEquipmentAlone() {
        Permanent creature = addCreature(player2, new AvatarOfMight());
        Permanent equipment = addEquipment(creature);

        cast(List.of(creature.getId(), equipment.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getName().equals("Leonin Scimitar"));
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addEquipment(Permanent attachedTo) {
        Permanent equipment = new Permanent(new LeoninScimitar());
        if (attachedTo != null) {
            equipment.setAttachedTo(attachedTo.getId());
        }
        gd.playerBattlefields.get(player2.getId()).add(equipment);
        return equipment;
    }

    private void cast(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new FieryAnnihilation()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, targetIds);
    }
}
