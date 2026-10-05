package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OdricMasterTactician.class, WalkingCorpse.class, AvenSquire.class, Unsummon.class})
class OdricMasterTacticianTest extends BaseCardTest {

    private Permanent addOdric() {
        return addCreatureReady(player1, new OdricMasterTactician());
    }

    private Permanent addAlly() {
        return addCreatureReady(player1, new WalkingCorpse());
    }

    private Permanent addDefender() {
        return addCreatureReady(player2, new WalkingCorpse());
    }

    /** Resolves attack triggers, then advances into the declare-blockers prompt. */
    private void advanceToBlockerDeclaration() {
        resolveAllTriggers();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Odric + 3 others attacking lets the controller declare blocks")
    void odricAndThreeOthersLetsControllerDeclareBlocks() {
        Permanent odric = addOdric();
        Permanent ally1 = addAlly();
        Permanent ally2 = addAlly();
        Permanent ally3 = addAlly();
        Permanent blocker = addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.defenderId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isTrue();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int odricIdx = gd.playerBattlefields.get(player1.getId()).indexOf(odric);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIdx, odricIdx)));

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(odric.getId());
        assertThat(ally1.isAttacking()).isTrue();
        assertThat(ally2.isAttacking()).isTrue();
        assertThat(ally3.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Defender cannot declare blocks while Odric's ability is in force")
    void defenderCannotDeclareBlocks() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger when Odric attacks with only two other creatures")
    void doesNotTriggerWithFewerAllies() {
        addOdric();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(0, 1, 2));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when four creatures attack but Odric does not")
    void doesNotTriggerWhenOdricDoesNotAttack() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        addAlly();
        addDefender();

        declareAttackers(List.of(1, 2, 3, 4));
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(pending.choosingForOpponent()).isFalse();
    }

    @Test
    @DisplayName("Odric's controller may choose no blockers")
    void controllerCanChooseNoBlocks() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        Permanent blocker = addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();
        gs.declareBlockers(gd, player1, List.of());

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
    }

    @Test
    @DisplayName("Odric cannot make a ground creature block a flying attacker")
    void controllerMustRespectFlyingRestriction() {
        addOdric();
        addCreatureReady(player1, new AvenSquire());
        addAlly();
        addAlly();
        Permanent blocker = addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
        gs.declareBlockers(gd, player1, List.of());
    }

    @Test
    @DisplayName("Odric cannot make a tapped creature block")
    void controllerCannotChooseTappedBlocker() {
        addOdric();
        addAlly();
        addAlly();
        addAlly();
        Permanent tappedBlocker = addDefender();
        tappedBlocker.setTapped(true);
        addDefender();

        declareAttackers(List.of(0, 1, 2, 3));
        advanceToBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tappedBlocker.isBlocking()).isFalse();
        gs.declareBlockers(gd, player1, List.of());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Removing Odric or an ally in response does not undo the attack trigger")
    void triggerSurvivesAttackerRemoval(boolean removeOdric) {
        Permanent odric = addOdric();
        Permanent ally = addAlly();
        addAlly();
        addAlly();
        addDefender();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0, 1, 2, 3));
        harness.castAndResolveInstant(player2, 0, removeOdric ? odric.getId() : ally.getId());
        harness.assertInHand(player1, removeOdric ? "Odric, Master Tactician" : "Walking Corpse");
        advanceToBlockerDeclaration();

        PendingInteraction.BlockerDeclaration pending =
                gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        assertThat(pending).isNotNull();
        assertThat(pending.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(pending.choosingForOpponent()).isTrue();
        gs.declareBlockers(gd, player1, List.of());
    }
}
