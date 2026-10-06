package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RotWolf;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.d.DivineOffering;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skinwing.class, RotWolf.class, GoForTheThroat.class, DivineOffering.class})
class SkinwingTest extends BaseCardTest {

    @Test
    @DisplayName("Equip requires six mana and targets a creature you control")
    void equipRequiresSixManaAndOwnCreature() {
        Permanent skinwing = harness.addToBattlefieldAndReturn(player1, new Skinwing());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new RotWolf());
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skinwing.getAttachedTo()).isNull();

        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.activateAbility(player1, 0, null, ownCreature.getId());
        assertThat(skinwing.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(skinwing.getAttachedTo()).isEqualTo(ownCreature.getId());
    }

    @Test
    @DisplayName("Casting Skinwing triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // After the artifact resolves, the living weapon ETB should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Skinwing");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        // Resolve artifact spell
        harness.passBothPriorities();
        // Resolve living weapon ETB trigger
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        // Should have Skinwing (equipment) and Phyrexian Germ (token)
        Permanent skinwing = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Skinwing"))
                .findFirst().orElseThrow();
        Permanent germ = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Germ"))
                .findFirst().orElseThrow();

        // Skinwing should be attached to the Germ token
        assertThat(skinwing.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

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
    @DisplayName("Germ token gets +2/+2 and flying from Skinwing")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 2/2 from equipment = 2/2 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equipping Skinwing to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Add a creature to equip to
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());

        // Equip to wolf
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        Permanent skinwing = findPermanent(player1, "Skinwing");

        assertThat(skinwing.getAttachedTo()).isEqualTo(wolf.getId());

        // Wolf should get +2/+2 and flying
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(4);  // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(4);  // 2 + 2
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Skinwing is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());

        // Equip to wolf — this moves the equipment, Germ becomes 0/0 and dies
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, wolf.getId());
        harness.passBothPriorities();

        // Germ should be dead (0 toughness, state-based action)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Skinwing stays on battlefield when Germ is removed")
    void equipmentStaysWhenGermIsRemoved() {
        harness.setHand(player1, List.of(new Skinwing()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, germ.getId());
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        assertThat(findPermanent(player1, "Skinwing").getAttachedTo()).isNull();

        // Equipment should still be on the battlefield
        harness.assertOnBattlefield(player1, "Skinwing");
    }

    @Test
    @DisplayName("Living weapon resolves after Skinwing is destroyed")
    void livingWeaponResolvesWithoutEquipment() {
        harness.setHand(player1, List.of(new Skinwing(), new DivineOffering()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent skinwing = findPermanent(player1, "Skinwing");

        harness.castAndResolveInstant(player1, 0, skinwing.getId());
        harness.assertInGraveyard(player1, "Skinwing");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        harness.assertNotOnBattlefield(player1, "Skinwing");
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresSorceryTiming() {
        Permanent skinwing = harness.addToBattlefieldAndReturn(player1, new Skinwing());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.forceStep(TurnStep.END_STEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wolf.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skinwing.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An equip target leaving keeps Skinwing attached to its Germ")
    void equipTargetLeavingPreservesAttachment() {
        harness.setHand(player1, List.of(new Skinwing(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        harness.activateAbility(player1, 0, null, wolf.getId());

        harness.castAndResolveInstant(player1, 0, wolf.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Skinwing").getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.FLYING)).isTrue();
        harness.assertNotOnBattlefield(player1, "Rot Wolf");
    }

}
