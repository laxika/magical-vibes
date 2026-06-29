package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagingSwordtoothTest extends BaseCardTest {

    @Test
    @DisplayName("Has ETB MassDamageEffect with 'each other creature' filter")
    void hasCorrectEtbEffect() {
        RagingSwordtooth card = new RagingSwordtooth();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(MassDamageEffect.class);
        MassDamageEffect effect = (MassDamageEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
        assertThat(effect.damagesPlayers()).isFalse();
        assertThat(effect.filter()).isNotNull();
    }

    @Test
    @DisplayName("Casting puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castSwordtooth();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Raging Swordtooth");
    }

    @Test
    @DisplayName("ETB deals 1 damage to opponent's 1/1 creature, killing it")
    void etbKillsOpponentOneOne() {
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        castSwordtooth();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB deals 1 damage to controller's own 1/1 creature, killing it")
    void etbKillsControllerOneOne() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        ownCreature.setPower(1);
        ownCreature.setToughness(1);
        harness.addToBattlefield(player1, ownCreature);

        castSwordtooth();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB does NOT deal damage to itself")
    void etbDoesNotDamageItself() {
        castSwordtooth();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Raging Swordtooth"));
        // Swordtooth is 5/5 — even if it took 1 damage it would survive,
        // but verify it has no damage at all
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Raging Swordtooth"))
                .findFirst().orElseThrow().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB damages creatures on both sides but not itself")
    void etbDamagesBothSidesExceptSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 own
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 opponent

        castSwordtooth();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        // Both Grizzly Bears survive (2/2 take 1 damage) but have 1 damage
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getMarkedDamage()).isEqualTo(1);
        // Swordtooth itself has no damage
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Raging Swordtooth"))
                .findFirst().orElseThrow().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("ETB does not deal damage to players")
    void etbDoesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSwordtooth();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private void castSwordtooth() {
        harness.setHand(player1, List.of(new RagingSwordtooth()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }
}
