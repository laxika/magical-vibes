package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PersuasionTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Persuasion has correct card properties")
    void hasCorrectProperties() {
        Persuasion card = new Persuasion();

        assertThat(card.getName()).isEqualTo("Persuasion");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{3}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ControlEnchantedCreatureEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Persuasion targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Persuasion");
        assertThat(entry.getTargetPermanentId()).isEqualTo(creature.getId());
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving Persuasion steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Persuasion aura should be on player1's battlefield attached to the creature
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Persuasion")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(creature.getId()));

        // Stolen creature should be summoning sick
        assertThat(creature.isSummoningSick()).isTrue();

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Persuasion fizzles if target creature is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // Persuasion should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Persuasion"));
    }

    @Test
    @DisplayName("Creature returns to owner when Persuasion is destroyed")
    void creatureReturnsWhenPersuasionDestroyed() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        // Player1 casts Persuasion, resolve it
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // Find the Persuasion aura permanent
        Permanent persuasionPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Persuasion"))
                .findFirst().orElseThrow();

        // Set up for Demystify: force step to a main phase, give player2 priority
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        // Player1 passes, player2 casts Demystify targeting Persuasion
        harness.passPriority(player1);
        harness.castInstant(player2, 0, persuasionPerm.getId());
        harness.passBothPriorities();

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Stolen creatures map should be cleaned up
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    // ===== Helper methods =====

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

