package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrimalCocoonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Primal Cocoon has upkeep counter and attack/block sacrifice effects")
    void hasCorrectEffects() {
        PrimalCocoon card = new PrimalCocoon();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(PutPlusOnePlusOneCounterOnEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    // ===== Upkeep +1/+1 counter =====

    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a +1/+1 counter")
    void upkeepPutsPlusCounter() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getPlusOnePlusOneCounters();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(countersBefore + 1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getPlusOnePlusOneCounters();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(countersBefore);
    }

    @Test
    @DisplayName("Counters accumulate over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    // ===== Sacrifice on attack =====

    @Test
    @DisplayName("Primal Cocoon is sacrificed when enchanted creature attacks")
    void sacrificedWhenEnchantedCreatureAttacks() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanentByName(player1, "Primal Cocoon")).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve sacrifice trigger
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Primal Cocoon"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Primal Cocoon"));
    }

    @Test
    @DisplayName("Creature keeps +1/+1 counters after Primal Cocoon is sacrificed")
    void creatureKeepsCountersAfterSacrifice() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new PrimalCocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Accumulate a counter
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Attack to trigger sacrifice
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities(); // resolve sacrifice trigger

        // Creature should still have the +1/+1 counter
        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(1);
        // Cocoon should be gone
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Primal Cocoon"));
    }

    // ===== Helper methods =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElse(null);
    }
}
