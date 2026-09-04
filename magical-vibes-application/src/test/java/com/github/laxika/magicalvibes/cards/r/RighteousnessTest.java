package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Righteousness.class, GrizzlyBears.class, Plains.class})
class RighteousnessTest extends BaseCardTest {



    @Test
    @DisplayName("Casting Righteousness targeting a blocking creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(Righteousness.class);
        assertThat(entry.getTargetId()).isEqualTo(blockerPerm.getId());
    }

    @Test
    @DisplayName("Cannot target a non-blocking creature")
    void cannotTargetNonBlockingCreature() {
        // Add a blocking creature as valid target so spell is playable
        Permanent blockerValid = addCreatureReady(player2, new GrizzlyBears());
        blockerValid.setBlocking(true);

        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it is marked as blocking")
    void cannotTargetBlockingNoncreature() {
        Permanent blockerValid = addCreatureReady(player1, new GrizzlyBears());
        blockerValid.setBlocking(true);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Plains());
        noncreature.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        // Add a blocking creature as valid target so spell is playable
        Permanent blockerValid = addCreatureReady(player1, new GrizzlyBears());
        blockerValid.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        Permanent blockerPerm = addCreatureReady(player1, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.setHand(player1, List.of(new Righteousness()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blockerPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }



    @Test
    @DisplayName("Resolving gives +7/+7 to target blocking creature")
    void resolvingGivesBoost() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);
        assertThat(blockerPerm.getPowerModifier()).isEqualTo(7);
        assertThat(blockerPerm.getToughnessModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blockerPerm.getPowerModifier()).isEqualTo(0);
        assertThat(blockerPerm.getToughnessModifier()).isEqualTo(0);
        assertThat(blockerPerm.getEffectivePower()).isEqualTo(2);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Righteousness goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Righteousness");
    }



    @Test
    @DisplayName("Righteousness fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Righteousness fizzles if target stops blocking before resolution")
    void fizzlesIfTargetStopsBlocking() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        blockerPerm.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blockerPerm.getEffectivePower()).isEqualTo(2);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }



    @Test
    @DisplayName("Boosted blocker survives combat with a large attacker")
    void boostedBlockerSurvivesCombat() {
        harness.setLife(player2, 20);

        // Player1 has a 5/5 attacker
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(5);
        bigCreature.setToughness(5);
        Permanent atkPerm = addCreatureReady(player1, bigCreature);
        atkPerm.setAttacking(true);

        // Player2 has a 2/2 blocker — set up blocking state manually
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        // Cast and resolve Righteousness before combat damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Righteousness()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blockerPerm.getId());
        harness.passBothPriorities();

        // Blocker is now 9/9 — verify boost applied
        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);

        // Advance to combat damage
        resolveCombat(player1);

        // Blocker should survive (9 toughness vs 5 damage), attacker should die (5 toughness vs 9 damage)
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Player2 takes no damage (attacker was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }



    @Test
    @DisplayName("Can target opponent's blocking creature")
    void canTargetOpponentsBlockingCreature() {
        Permanent blockerPerm = addCreatureReady(player2, new GrizzlyBears());
        blockerPerm.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Righteousness()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, blockerPerm.getId());
        harness.passBothPriorities();

        assertThat(blockerPerm.getEffectivePower()).isEqualTo(9);
        assertThat(blockerPerm.getEffectiveToughness()).isEqualTo(9);
    }
}

