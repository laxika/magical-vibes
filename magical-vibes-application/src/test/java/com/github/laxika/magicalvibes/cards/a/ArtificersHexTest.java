package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArtificersHexTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Artificer's Hex targeting a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        addEquipment(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArtificersHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an Equipment");
    }

    @Test
    @DisplayName("Casting Artificer's Hex on an Equipment attaches it to that Equipment")
    void resolvingAttachesToEquipment() {
        Permanent shield = addEquipment(player1);
        harness.setHand(player1, List.of(new ArtificersHex()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, shield.getId());
        harness.passBothPriorities();

        Permanent hex = findPermanent(player1, "Artificer's Hex");
        assertThat(hex.getAttachedTo()).isEqualTo(shield.getId());
    }

    @Test
    @DisplayName("Upkeep trigger destroys the creature the enchanted Equipment is attached to")
    void upkeepDestroysEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addEquipment(player1);
        shield.setAttachedTo(creature.getId());
        addHexOn(player1, shield);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Upkeep trigger destroys an opponent's creature wearing the enchanted Equipment")
    void upkeepDestroysOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent shield = addEquipment(player2);
        shield.setAttachedTo(creature.getId());
        addHexOn(player1, shield);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Nothing is destroyed when the enchanted Equipment is attached to no creature")
    void upkeepDoesNothingWhenEquipmentUnattached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addEquipment(player1);
        addHexOn(player1, shield);

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The trigger does not fire during the opponent's upkeep")
    void doesNotFireOnOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent shield = addEquipment(player1);
        shield.setAttachedTo(creature.getId());
        addHexOn(player1, shield);

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    private Permanent addEquipment(Player player) {
        Permanent perm = new Permanent(new AccordersShield());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addHexOn(Player player, Permanent equipment) {
        Permanent hex = new Permanent(new ArtificersHex());
        hex.setAttachedTo(equipment.getId());
        gd.playerBattlefields.get(player.getId()).add(hex);
        return hex;
    }
}
