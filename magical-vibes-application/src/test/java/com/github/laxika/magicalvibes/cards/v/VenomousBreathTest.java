package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WordOfUndoing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomousBreath.class, BalduvianBears.class, Forest.class, WordOfUndoing.class})
class VenomousBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature blocking the target is destroyed at end of combat, not on resolution")
    void destroysAllBlockersAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        castVenomousBreath(player1, attacker);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("A creature the target blocks is destroyed too")
    void destroysTheAttackerTheTargetBlocks() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        castVenomousBreath(player2, blocker);
        advanceThroughEndOfCombat();

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertOnBattlefield(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Nothing is destroyed when the target was never in a block this turn")
    void unblockedTargetDestroysNothing() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        castVenomousBreath(player1, attacker);
        advanceThroughEndOfCombat();

        harness.assertOnBattlefield(player2, "Balduvian Bears");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("A blocker declared after resolution is destroyed at the next end of combat")
    void destroysBlockerDeclaredAfterResolution() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castVenomousBreath(player1, attacker);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Casting after this turn's end of combat does not destroy creatures at a later end of combat")
    void doesNotCarryDelayedDestructionIntoLaterTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        advanceThroughEndOfCombat();

        castVenomousBreath(player1, attacker);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.DECLARE_ATTACKERS);
        declareAttackers(player2, List.of());
        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("A land can't be targeted")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VenomousBreath()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing the chosen creature after resolution does not save its blockers")
    void destroysBlockersAfterTargetLeaves() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        castVenomousBreath(player1, attacker);

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.castAndResolveInstant(player1, 0, attacker.getId()));
        harness.assertInHand(player1, "Balduvian Bears");

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Removing the chosen creature before resolution prevents delayed destruction")
    void targetLeavingBeforeResolutionPreventsDestruction() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.setHand(player1, List.of(new VenomousBreath()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, attacker.getId());
        harness.setHand(player2, List.of(new WordOfUndoing()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, attacker.getId());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("The end-of-combat destruction uses the stack and allows a response")
    void canRespondToDelayedDestruction() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        castVenomousBreath(player1, attacker);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new WordOfUndoing()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.withAutoStop(TurnStep.END_OF_COMBAT,
                () -> harness.castAndResolveInstant(player2, 0, blocker.getId()));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, this::resolveAllTriggers);

        harness.assertInHand(player2, "Balduvian Bears");
        harness.assertNotInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Casting during the end-of-combat step cannot trigger at that step's end")
    void castingDuringEndOfCombatWaitsForAnotherCombat() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        castVenomousBreath(player1, attacker);
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creatures from an earlier combat this turn are also destroyed")
    void includesOpponentsFromEarlierCombatThisTurn() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        advanceThroughEndOfCombat();

        gd.additionalCombatPhasesAfterMain = 1;
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        castVenomousBreath(player1, attacker);
        advanceThroughEndOfCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    private void castVenomousBreath(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new VenomousBreath()));
        harness.addMana(caster, ManaColor.GREEN, 4);
        harness.withAutoStop(gd.currentStep,
                () -> harness.castAndResolveInstant(caster, 0, target.getId()));
    }

    private void advanceThroughEndOfCombat() {
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
    }

}
