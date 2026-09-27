package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkmageApprentice.class, BorosRecruit.class, SelesnyaGuildmage.class,
        ShalaiVoiceOfPlenty.class, ChandraNalaar.class})
class SparkmageApprenticeTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sparkmage Apprentice targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.castCreature(player1, 0, targetId);

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

        harness.castCreature(player1, 0, player2.getId());

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
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

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
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        harness.assertInGraveyard(player2, "Boros Recruit");
    }

    @Test
    @DisplayName("ETB deals 1 damage to a 2/2 creature but does not kill it")
    void etbDeals1DamageDoesNotKillTwoTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildmage());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, target.getId());

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Selesnya Guildmage");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    // ===== Damage to player =====

    @Test
    @DisplayName("ETB deals 1 damage to target player")
    void etbDeals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, player2.getId());

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB deals 1 damage to a target planeswalker")
    void etbDeals1DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no valid targets exist")
    void canCastWithoutTarget() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sparkmage Apprentice");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Hexproof interaction (Shalai board state) =====

    @Test
    @DisplayName("Can cast Sparkmage Apprentice without target when opponent has hexproof from Shalai")
    void canCastWithoutTargetWhenOpponentHasHexproof() {
        // Reproduce fuzz test board state: opponent controls Shalai, Voice of Plenty
        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());

        // Verify opponent has hexproof
        assertThat(gqs.playerHasHexproof(gd, player2.getId())).isTrue();

        // Cast without a target — creature spell itself doesn't target
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        // Resolve creature spell
        harness.passBothPriorities();

        // Creature enters battlefield, ETB skipped (no target provided)
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");
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
        harness.castCreature(player1, 0, player2.getId());

        // Resolve creature spell → enters battlefield, ETB triggers with hexproof player target
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

        // Resolve ETB → fizzles because target player has hexproof
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
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
        harness.castCreature(player1, 0, player1.getId());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = target.getId();

        // Cast targeting hexproof creature — spell itself doesn't target
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

        // Resolve ETB → fizzles because target creature has hexproof
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Boros Recruit");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new BorosRecruit());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
