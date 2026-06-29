package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SparkmageApprenticeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sparkmage Apprentice has correct card properties")
    void hasCorrectProperties() {
        SparkmageApprentice card = new SparkmageApprentice();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sparkmage Apprentice targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sparkmage Apprentice");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Casting Sparkmage Apprentice targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, player2.getId(), null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sparkmage Apprentice");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("Resolving Sparkmage Apprentice enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparkmage Apprentice"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Sparkmage Apprentice");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    // ===== Damage to creature =====

    @Test
    @DisplayName("ETB deals 1 damage to target creature, killing a 1/1")
    void etbDeals1DamageToCreatureKillsOneOne() {
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB deals 1 damage to a 2/2 creature but does not kill it")
    void etbDeals1DamageDoesNotKillTwoTwo() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Damage to player =====

    @Test
    @DisplayName("ETB deals 1 damage to target player")
    void etbDeals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, player2.getId(), null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no valid targets exist")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sparkmage Apprentice");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparkmage Apprentice"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Hexproof interaction (Shalai board state) =====

    @Test
    @DisplayName("Can cast Sparkmage Apprentice without target when opponent has hexproof from Shalai")
    void canCastWithoutTargetWhenOpponentHasHexproof() {
        // Reproduce fuzz test board state: opponent controls Shalai, Voice of Plenty
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Verify opponent has hexproof
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isTrue();

        // Cast without a target — creature spell itself doesn't target
        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        // Creature enters battlefield, ETB skipped (no target provided)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparkmage Apprentice"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles when targeting hexproof opponent (Shalai on battlefield)")
    void etbFizzlesWhenTargetingHexproofOpponent() {
        // Reproduce fuzz test board state: opponent controls Shalai, Voice of Plenty
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Cast targeting the hexproof opponent — spell itself doesn't target, so cast succeeds
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, player2.getId(), null);

        // Resolve creature spell → enters battlefield, ETB triggers with hexproof player target
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparkmage Apprentice"));

        // Resolve ETB → fizzles because target player has hexproof
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("ETB still works targeting self when opponent has hexproof from Shalai")
    void etbCanTargetSelfWhenOpponentHasHexproof() {
        // Opponent controls Shalai but caster can still target themselves
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        // Target self — hexproof only blocks opponents
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, player1.getId(), null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB → deals 1 damage to self
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB fizzles when targeting hexproof creature (Shalai grants hexproof to other creatures)")
    void etbFizzlesWhenTargetingHexproofCreature() {
        // Shalai grants hexproof to other creatures controller controls
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Cast targeting hexproof creature — spell itself doesn't target
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, bearsId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sparkmage Apprentice"));

        // Resolve ETB → fizzles because target creature has hexproof
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Grizzly Bears should still be alive (no damage dealt)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
