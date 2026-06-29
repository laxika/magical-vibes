package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinChainwhirlerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Goblin Chainwhirler does not need a target")
    void doesNotNeedTarget() {
        GoblinChainwhirler card = new GoblinChainwhirler();

        assertThat(EffectResolution.needsTarget(card)).isFalse();
    }

    @Test
    @DisplayName("Has two ETB effects: damage to opponents and damage to their creatures/planeswalkers")
    void hasEtbEffects() {
        GoblinChainwhirler card = new GoblinChainwhirler();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD))
                .hasAtLeastOneElementOfType(DealDamageToEachOpponentEffect.class)
                .hasAtLeastOneElementOfType(DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castChainwhirler();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chainwhirler");
    }

    // ===== ETB damage to opponent =====

    @Test
    @DisplayName("Resolving puts Goblin Chainwhirler on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Goblin Chainwhirler"));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chainwhirler");
    }

    @Test
    @DisplayName("ETB deals 1 damage to each opponent")
    void etbDeals1DamageToEachOpponent() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== ETB damage to creatures =====

    @Test
    @DisplayName("ETB deals 1 damage to each creature opponent controls, killing 1/1s")
    void etbKillsOneOneCreatures() {
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB deals 1 damage to opponent's 2/2 creature but does not kill it")
    void etbDoesNotKillTwoTwoCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB does not damage controller's creatures")
    void etbDoesNotDamageControllerCreatures() {
        GrizzlyBears ownCreature = new GrizzlyBears();
        ownCreature.setPower(1);
        ownCreature.setToughness(1);
        harness.addToBattlefield(player1, ownCreature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB damages multiple opponent creatures simultaneously")
    void etbDamagesMultipleCreatures() {
        GrizzlyBears creature1 = new GrizzlyBears();
        creature1.setPower(1);
        creature1.setToughness(1);
        GrizzlyBears creature2 = new GrizzlyBears();
        creature2.setPower(1);
        creature2.setToughness(1);
        harness.addToBattlefield(player2, creature1);
        harness.addToBattlefield(player2, creature2);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Combined damage =====

    @Test
    @DisplayName("ETB deals damage to opponent AND their creatures simultaneously")
    void etbDealsDamageToBothOpponentAndCreatures() {
        harness.setLife(player2, 20);
        GrizzlyBears creature = new GrizzlyBears();
        creature.setPower(1);
        creature.setToughness(1);
        harness.addToBattlefield(player2, creature);

        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Stack is clean after resolution =====

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        castChainwhirler();
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castChainwhirler() {
        harness.setHand(player1, List.of(new GoblinChainwhirler()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
    }
}
