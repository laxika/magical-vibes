package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MortalWoundTest extends BaseCardTest {

    private boolean onBattlefield(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .anyMatch(p -> p.getCard().getName().equals(name));
    }

    @Test
    @DisplayName("Resolving Mortal Wound attaches it to the target creature")
    void resolvingAttachesToCreature() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mortal Wound")
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Cannot cast Mortal Wound targeting a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = findPermanent(player1, "Swamp");
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, swamp.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(onBattlefield(player1, "Hill Giant")).isTrue();
        assertThat(giant).isNotNull();
    }

    @Test
    @DisplayName("Non-lethal noncombat damage to the enchanted creature destroys it")
    void noncombatDamageDestroysEnchantedCreature() {
        Permanent giant = addCreatureReady(player2, new HillGiant()); // 3/3 survives Shock
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities(); // Resolve Shock

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Mortal Wound"));
        while (gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Mortal Wound"))) {
            harness.passBothPriorities();
        }

        assertThat(onBattlefield(player2, "Hill Giant")).isFalse();
        assertThat(onBattlefield(player1, "Mortal Wound")).isFalse();
    }

    @Test
    @DisplayName("Combat damage to the enchanted creature destroys it")
    void combatDamageDestroysEnchantedCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        Permanent blocker = addCreatureReady(player2, new HillGiant()); // 3/3 survives combat
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, blocker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        blocker.setBlocking(true);
        // Attacker is index 0 on player1; Mortal Wound (Aura) is index 1 after casting.
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(onBattlefield(player2, "Hill Giant")).isFalse();
    }

    @Test
    @DisplayName("Damage to a different creature does not destroy the enchanted one")
    void damageToOtherCreatureDoesNotTrigger() {
        Permanent enchanted = addCreatureReady(player2, new HillGiant());
        Permanent other = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, other.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(onBattlefield(player2, "Hill Giant")).isTrue();
        assertThat(onBattlefield(player1, "Mortal Wound")).isTrue();
        assertThat(onBattlefield(player2, "Grizzly Bears")).isFalse();
    }
}
