package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({Strandwalker.class, SpinEngine.class, GoForTheThroat.class, DivineOffering.class})
class StrandwalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires four mana and does not tap Strandwalker")
    void hasEquipAbility() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Strandwalker());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());
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
    @DisplayName("Casting Strandwalker triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Strandwalker");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve living weapon ETB trigger
        harness.passBothPriorities();

        Permanent strandwalker = findPermanent(player1, "Strandwalker");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // Strandwalker should be attached to the Germ token
        assertThat(strandwalker.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

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
    @DisplayName("Germ token gets +2/+4 and reach from Strandwalker")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 2/4 from equipment = 2/4 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equipping Strandwalker to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());

        // Equip to the other creature
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        Permanent strandwalker = findPermanent(player1, "Strandwalker");

        assertThat(strandwalker.getAttachedTo()).isEqualTo(creature.getId());

        // The creature should get +2/+4 and reach
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Strandwalker is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());

        // Equip to the other creature — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Strandwalker stays on battlefield when Germ is removed")
    void equipmentStaysWhenGermIsRemoved() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, germ.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");

        // Equipment should still be on the battlefield
        harness.assertOnBattlefield(player1, "Strandwalker");
        assertThat(findPermanent(player1, "Strandwalker").getAttachedTo()).isNull();
    }

    @Test
    void equipRejectsOpponentCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Strandwalker());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpinEngine());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipRequiresEmptyStack() {
        harness.setHand(player1, List.of(new Strandwalker()));
        harness.addMana(player1, ManaColor.WHITE, 9);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinEngine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Strandwalker").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Phyrexian Germ").getId());
    }

    @Test
    void germDiesWhenEquipmentDestroyed() {
        harness.setHand(player1, List.of(new Strandwalker(), new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Strandwalker"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Strandwalker");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    void livingWeaponResolvesWhenEquipmentIsDestroyedInResponse() {
        harness.setHand(player1, List.of(new Strandwalker(), new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Strandwalker"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Strandwalker");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        assertThat(gd.stack).isEmpty();
    }
}
