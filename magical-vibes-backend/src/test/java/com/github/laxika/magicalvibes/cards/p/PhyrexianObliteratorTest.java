package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianObliteratorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Phyrexian Obliterator has one ON_DEALT_DAMAGE sacrifice effect")
    void hasCorrectEffect() {
        PhyrexianObliterator card = new PhyrexianObliterator();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst())
                .isInstanceOf(DamageSourceControllerSacrificesPermanentsEffect.class);
    }

    // ===== Non-combat damage trigger =====

    @Test
    @DisplayName("Shock dealing 2 damage to Obliterator forces source controller to sacrifice 2 permanents")
    void spellDamageForcesSourceControllerToSacrifice() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castInstant(player1, 0, obliteratorId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage to Obliterator

        GameData gd = harness.getGameData();

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has exactly 2 permanents, so both are auto-sacrificed
        harness.passBothPriorities();

        // Both Grizzly Bears should be sacrificed (battlefield empty)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // Obliterator should survive (5/5 takes only 2 damage)
        harness.assertOnBattlefield(player2, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("When source controller has more permanents than damage, they choose which to sacrifice")
    void spellDamagePromptsChoiceWhenMorePermanents() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castInstant(player1, 0, obliteratorId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has 3 permanents but must sacrifice 2
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.pendingForcedSacrificeCount).isEqualTo(2);
        assertThat(gd.pendingForcedSacrificePlayerId).isEqualTo(player1.getId());

        // Player1 chooses which two to sacrifice
        List<Permanent> p1Battlefield = gd.playerBattlefields.get(player1.getId());
        UUID first = p1Battlefield.get(0).getId();
        UUID second = p1Battlefield.get(1).getId();
        harness.handleMultiplePermanentsChosen(player1, List.of(first, second));

        // Two sacrificed, one remains
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        // Graveyard has Shock + 2 sacrificed Grizzly Bears
        long sacrificedBears = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(sacrificedBears).isEqualTo(2);
    }

    @Test
    @DisplayName("When source controller has no permanents, nothing happens")
    void noPermanentsToSacrifice() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID obliteratorId = harness.getPermanentId(player2, "Phyrexian Obliterator");
        harness.castInstant(player1, 0, obliteratorId);
        harness.passBothPriorities(); // Resolve Shock

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);

        // Resolve the trigger — player1 has no permanents
        harness.passBothPriorities();

        // Nothing to sacrifice, game continues normally
        assertThat(gd.stack).isEmpty();
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Blocking creature dealing combat damage forces attacker's controller to sacrifice permanents")
    void combatDamageForcesAttackerControllerToSacrifice() {
        harness.addToBattlefield(player2, new PhyrexianObliterator());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2 attacker
        harness.addToBattlefield(player1, new GrizzlyBears()); // extra permanent

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).get(0);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent obliterator = gd.playerBattlefields.get(player2.getId()).getFirst();
        obliterator.setSummoningSick(false);
        obliterator.setBlocking(true);
        obliterator.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Resolve combat damage — attacker (2/2) deals 2 to Obliterator, Obliterator (5/5) kills attacker
        // Obliterator trigger goes on stack for 2 damage
        harness.passBothPriorities();

        // After combat, attacker dies (lethal from Obliterator), trigger on stack
        // One Grizzly Bears remains. Sacrifice count = 2, but only 1 permanent → auto-sacrifice that one
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Player1 should have no permanents (attacker died in combat + remaining auto-sacrificed)
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        // Obliterator should survive (5/5 takes 2 damage)
        harness.assertOnBattlefield(player2, "Phyrexian Obliterator");
    }

    @Test
    @DisplayName("Obliterator as attacker blocked by creature triggers sacrifice on blocker's controller")
    void obliteratorAttackingBlockedTriggersOnBlockerController() {
        harness.addToBattlefield(player1, new PhyrexianObliterator());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 blocker
        harness.addToBattlefield(player2, new GrizzlyBears()); // additional permanent
        harness.addToBattlefield(player2, new GrizzlyBears()); // additional permanent

        Permanent obliterator = gd.playerBattlefields.get(player1.getId()).getFirst();
        obliterator.setSummoningSick(false);
        obliterator.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).get(0);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Advance from DECLARE_BLOCKERS — Obliterator has trample so manual damage assignment needed
        harness.passBothPriorities();

        // Assign Obliterator's 5 damage: 2 to blocker (lethal for 2/2), 3 trample to player2
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        GameData gd = harness.getGameData();

        // Obliterator trigger fires: Grizzly Bears dealt 2 to Obliterator → player2 sacrifices 2
        // Blocker died in combat, player2 has 2 remaining → auto-sacrifice both
        // Resolve combat damage triggers
        harness.passBothPriorities();

        // All of player2's permanents should be gone (blocker died + 2 sacrificed)
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        // Obliterator should survive (5/5 takes 2 damage)
        harness.assertOnBattlefield(player1, "Phyrexian Obliterator");
    }
}
