package com.github.laxika.magicalvibes.cards.c;

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
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorruptedConscienceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Corrupted Conscience has correct effects")
    void hasCorrectEffects() {
        CorruptedConscience card = new CorruptedConscience();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof ControlEnchantedCreatureEffect)
                .hasSize(1);

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
    @DisplayName("Casting Corrupted Conscience targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Corrupted Conscience");
        assertThat(entry.getTargetPermanentId()).isEqualTo(creature.getId());
    }

    // ===== Resolution: control =====

    @Test
    @DisplayName("Resolving Corrupted Conscience steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Corrupted Conscience aura should be on player1's battlefield attached to the creature
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Corrupted Conscience")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(creature.getId()));

        // Stolen creature should be summoning sick
        assertThat(creature.isSummoningSick()).isTrue();

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    // ===== Resolution: infect keyword =====

    @Test
    @DisplayName("Enchanted creature has infect")
    void enchantedCreatureHasInfect() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();
    }

    // ===== Infect: combat damage =====

    @Test
    @DisplayName("Stolen creature deals combat damage as poison counters to defending player")
    void infectDealsPoisonCountersToPlayer() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Now creature is on player1's side with infect, attack player2
        creature.setSummoningSick(false);
        creature.setAttacking(true);

        resolveCombat();

        // Grizzly Bears has 2 power, infect deals poison counters
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        // Player2 life should be unchanged (infect deals poison, not life loss)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Stolen creature with infect deals combat damage as -1/-1 counters to blocking creature")
    void infectDealsMinusCountersToCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Stolen creature attacks
        creature.setSummoningSick(false);
        creature.setAttacking(true);

        // Find the attacker's index on player1's battlefield
        List<Permanent> atkBf = gd.playerBattlefields.get(player1.getId());
        int attackerIndex = atkBf.indexOf(creature);

        // Player2 blocks with a creature
        Permanent blocker = addCreatureReady(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(attackerIndex);

        resolveCombat();

        // Attacker has 2 power with infect → puts 2 -1/-1 counters on blocker
        // Blocker was 2/2, now 0/0 → dead (SBA)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Corrupted Conscience fizzles if target creature is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // Corrupted Conscience should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Conscience"));
    }

    // ===== Aura destruction: creature returns and loses infect =====

    @Test
    @DisplayName("Creature returns to owner and loses infect when Corrupted Conscience is destroyed")
    void creatureReturnsAndLosesInfectWhenDestroyed() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        // Cast and resolve Corrupted Conscience
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be on player1's side with infect
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isTrue();

        // Find the Corrupted Conscience aura permanent
        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Corrupted Conscience"))
                .findFirst().orElseThrow();

        // Destroy the aura with Demystify
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Stolen creatures map should be cleaned up
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());

        // Creature should no longer have infect
        assertThat(gqs.hasKeyword(gd, creature, Keyword.INFECT)).isFalse();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Corrupted Conscience")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CorruptedConscience()));
        harness.addMana(player1, ManaColor.BLUE, 5);

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
