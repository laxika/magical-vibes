package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyresisTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phyresis has correct effects")
    void hasCorrectEffects() {
        Phyresis card = new Phyresis();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);

        List<GrantKeywordEffect> keywordEffects = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof GrantKeywordEffect)
                .map(e -> (GrantKeywordEffect) e)
                .toList();
        assertThat(keywordEffects).hasSize(1);
        assertThat(keywordEffects.getFirst().keyword()).isEqualTo(Keyword.INFECT);
        assertThat(keywordEffects.getFirst().scope()).isEqualTo(GrantScope.ENCHANTED_CREATURE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Phyresis targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Phyresis");
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
    }

    // ===== Resolution: infect keyword =====

    @Test
    @DisplayName("Enchanted creature has infect")
    void enchantedCreatureHasInfect() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    @Test
    @DisplayName("Phyresis does not steal the creature")
    void doesNotStealCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature stays on player2's side
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Enchanted creature still has infect
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    // ===== Infect: combat damage =====

    @Test
    @DisplayName("Enchanted creature deals combat damage as poison counters to defending player")
    void infectDealsPoisonCountersToPlayer() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Attack with enchanted creature
        creature.setSummoningSick(false);
        creature.setAttacking(true);

        resolveCombat();

        // Grizzly Bears has 2 power, infect deals poison counters
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        // Player2 life should be unchanged
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature with infect deals combat damage as -1/-1 counters to blocking creature")
    void infectDealsMinusCountersToCreature() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Attack with enchanted creature
        creature.setSummoningSick(false);
        creature.setAttacking(true);

        // Find attacker index on player1's battlefield
        List<Permanent> atkBf = gd.playerBattlefields.get(player1.getId());
        int attackerIndex = atkBf.indexOf(creature);

        // Player2 blocks with a creature
        Permanent blocker = addCreatureReady(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(attackerIndex);

        resolveCombat();

        // Attacker has 2 power with infect -> 2 -1/-1 counters on blocker
        // Blocker was 2/2, now 0/0 -> dead (SBA)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Phyresis fizzles if target creature is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player1.getId()).remove(creature);

        harness.passBothPriorities();

        // Phyresis should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Phyresis"));
    }

    // ===== Aura destruction: creature loses infect =====

    @Test
    @DisplayName("Creature loses infect when Phyresis is destroyed")
    void creatureLosesInfectWhenAuraDestroyed() {
        Permanent creature = addCreatureReady(player1);

        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should have infect
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();

        // Find the Phyresis aura permanent
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyresis"))
                .findFirst().orElseThrow();

        // Destroy the aura with Demystify
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        // Creature should no longer have infect
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();

        // Creature should still be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Phyresis")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Phyresis()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
