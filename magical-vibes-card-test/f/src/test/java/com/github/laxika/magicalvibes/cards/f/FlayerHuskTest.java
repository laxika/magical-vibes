package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.cards.c.ChatterfangSquirrelGeneral;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({FlayerHusk.class, OgreResister.class, DivineOffering.class, ChatterfangSquirrelGeneral.class})
class FlayerHuskTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires two mana and resolves without tapping the Equipment")
    void equipRequiresTwoMana() {
        Permanent husk = harness.addToBattlefieldAndReturn(player1, new FlayerHusk());
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(husk.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        assertThat(husk.getAttachedTo()).isEqualTo(ogre.getId());
        assertThat(husk.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting Flayer Husk triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Flayer Husk");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve living weapon ETB trigger
        harness.passBothPriorities();

        Permanent flayerHusk = findPermanent(player1, "Flayer Husk");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // Flayer Husk should be attached to the Germ token
        assertThat(flayerHusk.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

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
    @DisplayName("Germ token gets +1/+1 from Flayer Husk")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 1/1 from equipment = 1/1 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipping Flayer Husk to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        // Equip to ogre
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        Permanent flayerHusk = findPermanent(player1, "Flayer Husk");

        assertThat(flayerHusk.getAttachedTo()).isEqualTo(ogre.getId());

        // Ogre should get +1/+1
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(5);  // 4 + 1
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(4);  // 3 + 1
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Flayer Husk is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        // Equip to ogre — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        Permanent husk = harness.addToBattlefieldAndReturn(player1, new FlayerHusk());
        Permanent ogre = harness.addToBattlefieldAndReturn(player2, new OgreResister());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(husk.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated while living weapon is on the stack")
    void equipRequiresEmptyStack() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new OgreResister());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ogre.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Flayer Husk").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Phyrexian Germ").getId());
    }

    @Test
    @DisplayName("Destroying Flayer Husk before living weapon resolves leaves no surviving Germ")
    void equipmentRemovedBeforeLivingWeaponResolves() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent husk = findPermanent(player1, "Flayer Husk");
        harness.setHand(player2, List.of(new DivineOffering()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, husk.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flayer Husk");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        harness.assertNotOnBattlefield(player2, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Destroying Flayer Husk removes its bonus and the Germ dies")
    void equipmentDestroyedAfterLivingWeaponResolves() {
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent husk = findPermanent(player1, "Flayer Husk");
        harness.setHand(player2, List.of(new DivineOffering()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, husk.getId());

        harness.assertInGraveyard(player1, "Flayer Husk");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @CardUsed({FlayerHusk.class, ChatterfangSquirrelGeneral.class})
    @DisplayName("Living weapon lets its controller choose the Germ when Chatterfang adds a Squirrel")
    void livingWeaponAllowsChoosingAmongCreatedTokens() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new FlayerHusk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent squirrel = findPermanent(player1, "Squirrel");
        harness.handlePermanentChosen(player1, germ.getId());

        assertThat(findPermanent(player1, "Flayer Husk").getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(1);
    }
}
