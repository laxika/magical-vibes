package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sickleslicer.class, PorcelainLegionnaire.class, BeastWithin.class})
class SickleslicerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires four mana and does not tap Sickleslicer")
    void equipRequiresFourMana() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Sickleslicer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(equipment.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(equipment.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting Sickleslicer triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Sickleslicer");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sickleslicer = findPermanent(player1, "Sickleslicer");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(sickleslicer.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);
    }

    @Test
    @DisplayName("Germ token gets +2/+2 from Sickleslicer")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 2/2 from equipment = 2/2 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipping Sickleslicer to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());

        // Equip to the creature
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent sickleslicer = findPermanent(player1, "Sickleslicer");

        assertThat(sickleslicer.getAttachedTo()).isEqualTo(creature.getId());

        // The creature gets +2/+2
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Sickleslicer is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());

        // Equip to the creature — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Sickleslicer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires an empty stack")
    void equipRequiresEmptyStack() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Sickleslicer").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Phyrexian Germ").getId());
    }

    @Test
    @DisplayName("Equip cannot be activated on an opponent's turn")
    void equipRequiresControllersTurn() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Sickleslicer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip does not detach from the Germ when its target is destroyed in response")
    void equipTargetRemovedInResponse() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Sickleslicer");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PorcelainLegionnaire());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(germ.getId());
        harness.assertOnBattlefield(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
    }

    @Test
    @DisplayName("Living weapon still resolves after Sickleslicer is destroyed")
    void equipmentRemovedBeforeLivingWeaponResolves() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Sickleslicer");
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, equipment.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sickleslicer");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        harness.assertNotOnBattlefield(player2, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Destroying Sickleslicer removes its bonus and the Germ dies")
    void equipmentDestroyedAfterLivingWeaponResolves() {
        harness.setHand(player1, List.of(new Sickleslicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Sickleslicer");
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, equipment.getId());

        harness.assertInGraveyard(player1, "Sickleslicer");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }
}
