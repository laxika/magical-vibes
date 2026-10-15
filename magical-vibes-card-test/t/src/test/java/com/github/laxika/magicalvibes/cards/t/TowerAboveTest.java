package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TowerAbove.class, GrizzlyBears.class, FountainOfYouth.class})
class TowerAboveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants +4/+4, trample, and wither to the target")
    void grantsBoostTrampleAndWither() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TowerAbove()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.WITHER);
    }

    @Test
    @DisplayName("Boost and keywords wear off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TowerAbove()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, bearId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.WITHER);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TowerAbove()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("When the boosted creature attacks, target creature is forced to block it")
    void attackTriggerForcesTargetToBlock() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(bear);

        declareAttackers(player1, List.of(0));
        // The granted ON_ATTACK trigger requires a target creature to force into blocking.
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMustBlockIds()).contains(bear.getId());
    }

    @Test
    @DisplayName("Forced creature must block the boosted attacker")
    void forcedCreatureMustBlock() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(bear);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();

        // Declaring no blockers is illegal — the targeted creature must block.
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No attack trigger fires after the grant wears off at end of turn")
    void noTriggerAfterGrantWearsOff() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(bear);

        // Wear off the temporary grant.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        bear.setSummoningSick(false);
        declareAttackers(player1, List.of(0));

        // No target selection should be prompted — the granted trigger is gone.
        assertThat(gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Two resolutions grant independent attack triggers with different targets")
    void repeatedGrantsChooseSeparateBlockers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(attacker);
        grantTowerAbove(attacker);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, firstBlocker.getId());
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
            harness.handlePermanentChosen(player1, secondBlocker.getId());
            resolveAllTriggers();
        });

        assertThat(firstBlocker.getMustBlockIds()).contains(attacker.getId());
        assertThat(secondBlocker.getMustBlockIds()).contains(attacker.getId());
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger can target a tapped creature without forcing an illegal block")
    void tappedTargetDoesNotHaveToBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        blocker.tap();
        grantTowerAbove(attacker);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, blocker.getId());
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger can target a creature controlled by the attacker")
    void attackTriggerCanTargetOwnCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(attacker);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, ownCreature.getId());
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(ownCreature.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Granted trample deals excess damage to the player and wither puts counters on the blocker")
    void trampleAndWitherApplyToCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        grantTowerAbove(attacker);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, blocker.getId());
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player2, 16);
    }

    private void grantTowerAbove(Permanent target) {
        harness.setHand(player1, List.of(new TowerAbove()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

}
