package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FallenAskari;
import com.github.laxika.magicalvibes.cards.f.Fireblast;
import com.github.laxika.magicalvibes.cards.h.HopeCharm;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MortalWound.class, PantherWarriors.class, MagmaMine.class, Fireblast.class,
        FallenAskari.class, HopeCharm.class, Tremor.class})
class MortalWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mortal Wound attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new PantherWarriors());
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mortal Wound");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot cast Mortal Wound targeting a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new PantherWarriors());
        harness.addToBattlefield(player1, new MagmaMine());
        Permanent artifact = findPermanent(player1, "Magma Mine");
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        harness.assertOnBattlefield(player1, "Panther Warriors");
    }

    @Test
    @DisplayName("Non-lethal noncombat damage to the enchanted creature destroys it")
    void noncombatDamageDestroysEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new PantherWarriors()); // 6/3 survives Tremor
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // Resolve Tremor

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Mortal Wound"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Panther Warriors");
        harness.assertNotOnBattlefield(player1, "Mortal Wound");
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger Mortal Wound")
    void preventedDamageDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new PantherWarriors());
        creature.setDamagePreventionShield(1);
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Panther Warriors");
        harness.assertOnBattlefield(player1, "Mortal Wound");
    }

    @Test
    @DisplayName("Combat damage to the enchanted creature destroys it")
    void combatDamageDestroysEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new FallenAskari()); // 2/2
        Permanent blocker = addCreatureReady(player2, new PantherWarriors()); // 6/3 survives combat
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Panther Warriors");
    }

    @Test
    @DisplayName("Damage to a different creature does not destroy the enchanted one")
    void damageToOtherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new PantherWarriors());
        Permanent other = addCreatureReady(player2, new FallenAskari());
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Fireblast()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, other.getId());

        harness.assertOnBattlefield(player2, "Panther Warriors");
        harness.assertOnBattlefield(player1, "Mortal Wound");
        harness.assertNotOnBattlefield(player2, "Fallen Askari");
    }

    @Test
    @DisplayName("A pending trigger still destroys the creature if Mortal Wound leaves the battlefield")
    void pendingTriggerUsesLastEnchantedCreatureAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new PantherWarriors());
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mortal Wound");
        harness.setHand(player1, List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Mortal Wound"));
        harness.assertOnBattlefield(player1, "Mortal Wound");

        harness.setHand(player1, List.of(new HopeCharm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 2, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mortal Wound");
        harness.assertNotOnBattlefield(player2, "Panther Warriors");
    }
}
