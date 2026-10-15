package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlindZealot;
import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Necropouncer.class, BlindZealot.class, BeastWithin.class, VaporSnag.class})
class NecroprouncerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip costs two mana and does not tap Necropouncer")
    void equipCostsTwoManaWithoutTapping() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Necropouncer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindZealot());
        harness.addMana(player1, ManaColor.WHITE, 1);

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
    @DisplayName("Casting Necropouncer triggers living weapon ETB on the stack")
    void castingTriggersLivingWeapon() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necropouncer");
    }

    @Test
    @DisplayName("Resolving living weapon creates a Phyrexian Germ token and attaches equipment")
    void livingWeaponCreatesGermAndAttaches() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent necropouncer = findPermanent(player1, "Necropouncer");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(necropouncer.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Phyrexian Germ token has correct properties")
    void germTokenHasCorrectProperties() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(germ.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(germ.getCard().getPower()).isEqualTo(0);
        assertThat(germ.getCard().getToughness()).isEqualTo(0);
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(germ.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.GERM);
    }

    @Test
    @DisplayName("Germ token gets +3/+1 and haste from Necropouncer")
    void germGetsEquipmentBonuses() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        // 0/0 base + 3/1 from equipment = 3/1 effective
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipping Necropouncer to another creature moves it from the Germ")
    void equipToAnotherCreature() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new BlindZealot());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, zealot.getId());
        harness.passBothPriorities();

        Permanent necropouncer = findPermanent(player1, "Necropouncer");

        assertThat(necropouncer.getAttachedTo()).isEqualTo(zealot.getId());

        // Zealot should get +3/+1 and haste
        assertThat(gqs.getEffectivePower(gd, zealot)).isEqualTo(5);  // 2 + 3
        assertThat(gqs.getEffectiveToughness(gd, zealot)).isEqualTo(3);  // 2 + 1
        assertThat(gqs.hasKeyword(gd, zealot, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Germ token dies (0 toughness) when Necropouncer is moved to another creature")
    void germDiesWhenEquipmentMoved() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent zealot = harness.addToBattlefieldAndReturn(player1, new BlindZealot());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, zealot.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Necropouncer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BlindZealot());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void equipRejectsNoncreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Necropouncer());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated while living weapon is on the stack")
    void equipRequiresEmptyStack() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 8);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindZealot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Necropouncer").getAttachedTo())
                .isEqualTo(findPermanent(player1, "Phyrexian Germ").getId());
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void equipRequiresControllersTurn() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Necropouncer());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindZealot());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.stack).isEmpty();
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Destroying the Equipment before living weapon resolves leaves no surviving Germ")
    void equipmentRemovedBeforeLivingWeaponResolves() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Necropouncer");
        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, equipment.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Necropouncer");
        harness.assertNotOnBattlefield(player1, "Phyrexian Germ");
        harness.assertNotOnBattlefield(player2, "Phyrexian Germ");
    }

    @Test
    @DisplayName("Removing an equip target in response keeps the Equipment on its Germ")
    void equipTargetRemovedInResponse() {
        harness.setHand(player1, List.of(new Necropouncer()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent equipment = findPermanent(player1, "Necropouncer");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindZealot());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.setHand(player2, List.of(new VaporSnag()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Blind Zealot");
        assertThat(equipment.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, germ, Keyword.HASTE)).isTrue();
    }
}
