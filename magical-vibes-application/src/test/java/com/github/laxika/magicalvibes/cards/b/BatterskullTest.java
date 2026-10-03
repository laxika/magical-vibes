package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TurnStep;
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

@CardUsed({Batterskull.class, GrizzlyBears.class})
class BatterskullTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Batterskull triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry etb = gd.stack.getFirst();
        assertThat(etb.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(etb.getCard().getName()).isEqualTo("Batterskull");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent batterskull = findPermanent(player1, "Batterskull");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(batterskull.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
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
    @DisplayName("Germ token gets +4/+4, vigilance, and lifelink from Batterskull")
    void germGetsEquipmentBonuses() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 4/4 from equipment = 4/4 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, germ, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Equipping Batterskull to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Permanent batterskull = findPermanent(player1, "Batterskull");

        assertThat(batterskull.getAttachedTo()).isEqualTo(bears.getId());

        // Bears should get +4/+4, vigilance, and lifelink
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);  // 2 + 4
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);  // 2 + 4
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Batterskull is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Activating {3} ability returns Batterskull to hand")
    void returnToHandAbility() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Batterskull and Germ are on the battlefield
        harness.assertOnBattlefield(player1, "Batterskull");

        // Activate {3}: Return to hand
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Batterskull should be in hand
        harness.assertNotOnBattlefield(player1, "Batterskull");
        harness.assertInHand(player1, "Batterskull");
    }

    @Test
    @DisplayName("Germ dies when Batterskull is returned to hand")
    void germDiesWhenBatterskullReturnedToHand() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Activate {3}: Return to hand
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Germ should be dead (0/0 without equipment)
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Batterskull stays on battlefield when Germ is removed")
    void equipmentStaysWhenGermIsRemoved() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, germ));
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Batterskull");
        assertThat(findPermanent(player1, "Batterskull").getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returning Batterskull before living weapon resolves leaves no Germ alive")
    void returnInResponseToLivingWeapon() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Batterskull");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Batterskull");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning equipped Batterskull removes its bonuses from the surviving creature")
    void returningEquipmentRemovesBonuses() {
        harness.addToBattlefield(player1, new Batterskull());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Batterskull");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        harness.addToBattlefield(player1, new Batterskull());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        harness.addToBattlefield(player1, new Batterskull());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Return ability can be activated during an opponent's turn and costs three mana")
    void returnDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new Batterskull());
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Batterskull");
    }

    @Test
    @DisplayName("Equip spends five generic mana")
    void equipCostsFiveMana() {
        harness.addToBattlefield(player1, new Batterskull());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, 1, null, bears.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Batterskull").getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equipped Germ attacks without tapping and gains life from combat damage")
    void vigilanceAndLifelinkWorkInCombat() {
        harness.castFromHand(player1, new Batterskull(), "{5}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        germ.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        declareAttackers(List.of(1));
        assertThat(germ.isTapped()).isFalse();
        resolveCombat();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
