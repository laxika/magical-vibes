package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TowerAboveTest extends BaseCardTest {

    // ===== Boost / keyword grant =====

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

    // ===== Granted attack trigger: forced block =====

    @Test
    @DisplayName("When the boosted creature attacks, target creature is forced to block it")
    void attackTriggerForcesTargetToBlock() {
        Permanent bear = readyCreature(player1);
        Permanent blocker = readyCreature(player2);
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
        Permanent bear = readyCreature(player1);
        Permanent blocker = readyCreature(player2);
        grantTowerAbove(bear);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        Permanent bear = readyCreature(player1);
        readyCreature(player2);
        grantTowerAbove(bear);

        // Wear off the temporary grant.
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        bear.setSummoningSick(false);
        declareAttackers(player1, List.of(0));

        // No target selection should be prompted — the granted trigger is gone.
        assertThat(gd.interaction.activeInteraction(
                com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice.class)).isNull();
    }

    // ===== Helpers =====

    private void grantTowerAbove(Permanent target) {
        harness.setHand(player1, List.of(new TowerAbove()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent readyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
