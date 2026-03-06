package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarnifexDemonTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has ETB -1/-1 counters effect and one activated ability")
    void hasCorrectEffectsAndAbility() {
        CarnifexDemon card = new CarnifexDemon();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).singleElement()
                .isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect etb = (PutCountersOnSourceEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(etb.powerModifier()).isEqualTo(-1);
        assertThat(etb.toughnessModifier()).isEqualTo(-1);
        assertThat(etb.amount()).isEqualTo(2);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(RemoveCounterFromSourceCost.class);
                    assertThat(effects.get(1)).isInstanceOf(PutMinusOneMinusOneCounterOnEachOtherCreatureEffect.class);
                });
    }

    // ===== ETB: enters with two -1/-1 counters =====

    @Test
    @DisplayName("Enters the battlefield with two -1/-1 counters (6/6 becomes 4/4)")
    void entersWithTwoMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CarnifexDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent demon = findDemon(player1);

        assertThat(demon.getMinusOneMinusOneCounters()).isEqualTo(2);
        assertThat(demon.getEffectivePower()).isEqualTo(4);
        assertThat(demon.getEffectiveToughness()).isEqualTo(4);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activated ability puts -1/-1 counter on each other creature")
    void abilityPutsCountersOnOtherCreatures() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Own Grizzly Bears gets a -1/-1 counter
        Permanent ownBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(ownBears.getMinusOneMinusOneCounters()).isEqualTo(1);

        // Opponent's Grizzly Bears also gets a -1/-1 counter
        Permanent oppBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(oppBears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability does NOT put counter on Carnifex Demon itself")
    void abilityDoesNotAffectSelf() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Demon started with 2 counters, had 1 removed as cost = 1 remaining
        // Should NOT have gotten an additional counter from its own effect
        assertThat(demon.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability removes a -1/-1 counter from source as cost")
    void abilityRemovesCounterFromSource() {
        Permanent demon = addReadyDemon(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Started with 2 -1/-1 counters, removed 1 as cost
        assertThat(demon.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(demon.getEffectivePower()).isEqualTo(5);
        assertThat(demon.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Can activate ability twice to remove both counters")
    void canActivateTwice() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // First activation
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.getMinusOneMinusOneCounters()).isEqualTo(1);

        // Second activation
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.getMinusOneMinusOneCounters()).isEqualTo(0);
        assertThat(demon.getEffectivePower()).isEqualTo(6);
        assertThat(demon.getEffectiveToughness()).isEqualTo(6);

        // Opponent's Grizzly Bears (2/2) died from two -1/-1 counters (0/0 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Cannot activate without counters =====

    @Test
    @DisplayName("Cannot activate ability when no counters remain")
    void cannotActivateWithoutCounters() {
        Permanent demon = addReadyDemon(player1);
        demon.setMinusOneMinusOneCounters(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    // ===== No timing restriction (instant speed) =====

    @Test
    @DisplayName("Can activate ability during combat (instant speed)")
    void canActivateDuringCombat() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent oppBears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(oppBears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Non-creature permanents not affected =====

    @Test
    @DisplayName("Non-creature permanents are not affected by the ability")
    void nonCreaturePermanentsNotAffected() {
        Permanent demon = addReadyDemon(player1);
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.s.Spellbook());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Spellbook (artifact, not a creature) should have no -1/-1 counters
        Permanent spellbook = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();
        assertThat(spellbook.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    // ===== Helpers =====

    private Permanent addReadyDemon(Player player) {
        CarnifexDemon card = new CarnifexDemon();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setMinusOneMinusOneCounters(2);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findDemon(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Carnifex Demon"))
                .findFirst().orElseThrow();
    }
}
