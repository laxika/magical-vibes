package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlisteningOilTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Glistening Oil has infect grant, upkeep counter, and death return effects")
    void hasCorrectEffects() {
        GlisteningOil card = new GlisteningOil();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantKeywordEffect.class);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(PutMinusOneMinusOneCounterOnEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Infect grant =====

    @Test
    @DisplayName("Enchanted creature gains infect")
    void enchantedCreatureGainsInfect() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Creature loses infect when Glistening Oil is removed")
    void creatureLosesInfectWhenOilRemoved() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent oilPerm = findPermanentByName(player1, "Glistening Oil");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, oilPerm.getId());
        harness.passBothPriorities(); // resolve Demystify
        harness.passBothPriorities(); // resolve death trigger

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }

    // ===== Upkeep -1/-1 counter =====

    @Test
    @DisplayName("At controller's upkeep, enchanted creature gets a -1/-1 counter")
    void upkeepPutsMinusCounter() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getMinusOneMinusOneCounters();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(creature.getMinusOneMinusOneCounters()).isEqualTo(countersBefore + 1);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int countersBefore = creature.getMinusOneMinusOneCounters();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.getMinusOneMinusOneCounters()).isEqualTo(countersBefore);
    }

    @Test
    @DisplayName("Counters accumulate over multiple upkeeps")
    void countersAccumulateOverUpkeeps() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== Return to hand on death =====

    @Test
    @DisplayName("Glistening Oil returns to owner's hand when destroyed")
    void returnsToHandWhenDestroyed() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new GlisteningOil()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent oilPerm = findPermanentByName(player1, "Glistening Oil");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, oilPerm.getId());
        // Resolve Demystify — Glistening Oil goes to graveyard, death trigger goes on stack
        harness.passBothPriorities();
        // Resolve death trigger — returns Glistening Oil from graveyard to hand
        harness.passBothPriorities();

        // Glistening Oil should be in owner's hand, not in graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Glistening Oil"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Glistening Oil"));
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
                .findFirst().orElseThrow();
    }
}
