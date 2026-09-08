package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavingGrace;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrudgeSkeletons.class, Fireball.class, GrizzlyBears.class, HillGiant.class, Incinerate.class,
        SavingGrace.class, Shock.class})
class IncinerateTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Incinerate targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Incinerate targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 3 damage to target creature, destroying a 2/2")
    void deals3DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy a creature with toughness greater than 3")
    void doesNotDestroyHighToughnessCreature() {
        // Hill Giant is 3/3 — 3 damage equals toughness, so it IS destroyed
        // We need a creature with toughness > 3 to survive
        // Hill Giant 3/3 takes exactly 3, so it is destroyed. Let's boost a creature instead.
        Permanent hillGiant = addCreatureReady(player2, new HillGiant());
        hillGiant.setToughnessModifier(1); // effectively 3/4

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hillGiant.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Nonlethal Incinerate damage still prevents regeneration this turn")
    void nonlethalDamageStillPreventsRegeneration() {
        Permanent skelePerm = addCreatureReady(player2, new DrudgeSkeletons());
        skelePerm.setToughnessModifier(3);
        skelePerm.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 1, List.of(skelePerm.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Creature dealt lethal damage by Incinerate cannot be regenerated")
    void creatureCannotRegenerateFromIncinerate() {
        // Drudge Skeletons (1/1) with regeneration shield
        Permanent skelePerm = addCreatureReady(player2, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        // Drudge Skeletons should be destroyed despite regeneration shield
        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Creature dealt lethal damage by Incinerate cannot be regenerated even with multiple shields")
    void creatureCannotRegenerateEvenWithMultipleShields() {
        Permanent skelePerm = addCreatureReady(player2, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(3);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Fully prevented Incinerate damage does not stop regeneration this turn")
    void fullyPreventedDamageDoesNotStopRegeneration() {
        Permanent skelePerm = addCreatureReady(player2, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setDamagePreventionShield(3);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(skelePerm.getRegenerationShield()).isEqualTo(1);

        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 1, List.of(skelePerm.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(skelePerm.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Partially prevented Incinerate damage still prevents regeneration")
    void partiallyPreventedDamageStillPreventsRegeneration() {
        Permanent skelePerm = addCreatureReady(player2, new DrudgeSkeletons());
        skelePerm.setRegenerationShield(1);
        skelePerm.setDamagePreventionShield(2);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, skelePerm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Damage redirected to another creature still prevents its regeneration")
    void redirectedDamagePreventsRegeneration() {
        Permanent enchantedCreature = addCreatureReady(player1, new DrudgeSkeletons());
        enchantedCreature.setRegenerationShield(1);
        Permanent originalTarget = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SavingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, originalTarget.getId());

        assertThat(originalTarget.getMarkedDamage()).isZero();
        assertThat(enchantedCreature.getMarkedDamage()).isEqualTo(3);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, enchantedCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantedCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantedCreature.getCard());
    }

    @Test
    @DisplayName("Incinerate goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Incinerate");
    }
}
