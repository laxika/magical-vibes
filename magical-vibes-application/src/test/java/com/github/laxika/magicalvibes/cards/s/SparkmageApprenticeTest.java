package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({SparkmageApprentice.class, RagingGoblin.class, RuneclawBear.class,
        ShalaiVoiceOfPlenty.class, ChandraNalaar.class, DoomBlade.class})
class SparkmageApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("The creature spell has no target even when a creature can be targeted by its ETB")
    void castingWithCreatureOnBattlefieldPutsUntargetedSpellOnStack() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Sparkmage Apprentice is an untargeted creature spell")
    void castingPutsUntargetedCreatureSpellOnStack() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving Sparkmage Apprentice enters battlefield and triggers ETB")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB deals 1 damage to target creature, killing a 1/1")
    void etbDeals1DamageToCreatureKillsOneOne() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("ETB deals 1 damage to a 2/2 creature but does not kill it")
    void etbDeals1DamageDoesNotKillTwoTwo() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, target.getId());

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

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

    @Test
    @DisplayName("The creature spell can be cast without choosing an ETB target")
    void canCastWithoutTarget() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting without a target still requires choosing a mandatory ETB target")
    void etbRequiresTargetAfterEntering() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

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

        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId()).doesNotContain(player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB does not resolve if the targeted opponent gains hexproof in response")
    void etbFizzlesWhenTargetingHexproofOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gameLogContains("fizzles")).isTrue();
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

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("ETB does not resolve if the targeted creature gains hexproof in response")
    void etbFizzlesWhenTargetingHexproofCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = target.getId();

        harness.castCreature(player1, 0, targetId);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");

        harness.addToBattlefield(player2, new ShalaiVoiceOfPlenty());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new SparkmageApprentice()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Raging Goblin");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("An obsolete cast-time target must not become the ETB target")
    void choosesLegalEtbTargetIfPreselectedCreatureLeavesBeforeEntering() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new SparkmageApprentice(), new DoomBlade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Raging Goblin");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sparkmage Apprentice");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sparkmage Apprentice can target itself after entering")
    void etbCanTargetItsOwnSource() {
        harness.castFromHand(player1, new SparkmageApprentice(), "{1}{R}");
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Sparkmage Apprentice");

        harness.handlePermanentChosen(player1, sourceId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sparkmage Apprentice");
        harness.assertInGraveyard(player1, "Sparkmage Apprentice");
    }

    @Test
    @DisplayName("Removing Sparkmage Apprentice does not stop its ETB damage")
    void etbResolvesAfterSourceIsDestroyed() {
        harness.setHand(player1, List.of(new SparkmageApprentice(), new DoomBlade()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Sparkmage Apprentice");

        harness.castInstant(player1, 0, sourceId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Sparkmage Apprentice");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
