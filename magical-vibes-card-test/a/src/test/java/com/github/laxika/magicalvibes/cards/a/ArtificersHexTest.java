package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalonianTusker;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArtificersHex.class, AccordersShield.class, GrizzlyBears.class, KalonianTusker.class, Naturalize.class})
class ArtificersHexTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot cast Artificer's Hex targeting a non-Equipment permanent")
    void cannotTargetNonEquipment() {
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
        harness.passBothPriorities();

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
        harness.passBothPriorities();

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

    @Test
    @DisplayName("An unattached enchanted Equipment does not trigger the upkeep ability")
    void unattachedEquipmentDoesNotTrigger() {
        Permanent shield = addEquipment(player1);
        addHexOn(player1, shield);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destroying the Aura in response does not save the equipped creature")
    void removingAuraDoesNotStopTriggeredAbility() {
        Permanent creature = addCreatureReady(player1, new KalonianTusker());
        Permanent shield = addEquipment(player1);
        shield.setAttachedTo(creature.getId());
        Permanent hex = addHexOn(player1, shield);
        harness.setHand(player2, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, hex.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(hex.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kalonian Tusker"));
    }

    @Test
    @DisplayName("Detaching the Equipment before resolution saves the creature")
    void detachedEquipmentDoesNothingOnResolution() {
        Permanent creature = addCreatureReady(player1, new KalonianTusker());
        Permanent shield = addEquipment(player1);
        shield.setAttachedTo(creature.getId());
        addHexOn(player1, shield);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        shield.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("The ability destroys the creature wearing the Equipment at resolution")
    void destroysCurrentEquippedCreature() {
        Permanent original = addCreatureReady(player1, new KalonianTusker());
        Permanent replacement = addCreatureReady(player1, new KalonianTusker());
        Permanent shield = addEquipment(player1);
        shield.setAttachedTo(original.getId());
        addHexOn(player1, shield);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        shield.setAttachedTo(replacement.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(original.getId()))
                .noneMatch(p -> p.getId().equals(replacement.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(shield.getId()));
        assertThat(findPermanent(player1, "Artificer's Hex").getAttachedTo()).isEqualTo(shield.getId());
    }

    private Permanent addEquipment(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new AccordersShield());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addHexOn(Player player, Permanent equipment) {
        Permanent hex = harness.addToBattlefieldAndReturn(player, new ArtificersHex());
        hex.setAttachedTo(equipment.getId());
        return hex;
    }
}
