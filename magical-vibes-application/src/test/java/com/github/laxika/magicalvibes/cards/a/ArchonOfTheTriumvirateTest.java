package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArchonOfTheTriumvirateTest extends BaseCardTest {

    @Test
    @DisplayName("Attack detains two chosen nonland permanents")
    void attackDetainsTwoTargets() {
        Permanent archon = addReadyArchon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        attackAndDetain(List.of(bear.getId(), elves.getId()), archon);

        assertThatThrownBy(() -> declareAttack(bear))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThatThrownBy(() -> harness.tapPermanent(player2, indexOf(player2, elves)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can detain a noncreature nonland permanent")
    void canDetainArtifact() {
        Permanent archon = addReadyArchon();
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        attackAndDetain(List.of(fountain.getId()), archon);

        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, fountain), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Can choose zero targets (up to two)")
    void canChooseZeroTargets() {
        Permanent archon = addReadyArchon();
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);

        // Decline the optional first slot by choosing yourself.
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        Permanent bear = findPermanent(player2, "Grizzly Bears");
        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Detain wears off at the Archon controller's next turn")
    void detainWearsOffAtControllersNextTurn() {
        Permanent archon = addReadyArchon();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        attackAndDetain(List.of(bear.getId()), archon);
        gd.expireFloatingEffectsAtTurnStart(player1.getId());

        assertThatCode(() -> declareAttack(bear)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Cannot detain a permanent you control")
    void cannotDetainOwnPermanent() {
        Permanent archon = addReadyArchon();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        declareAttackers(List.of(indexOf(player1, archon)));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyArchon() {
        Permanent archon = harness.addToBattlefieldAndReturn(player1, new ArchonOfTheTriumvirate());
        archon.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        return archon;
    }

    private void attackAndDetain(List<java.util.UUID> targetIds, Permanent archon) {
        declareAttackers(List.of(indexOf(player1, archon)));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);

        for (java.util.UUID targetId : targetIds) {
            harness.handlePermanentChosen(player1, targetId);
        }
        // "Up to N": decline remaining slots only while the picker is still open. If no more
        // legal permanents remain, ETBTokenTargetService advances past the group automatically.
        if (targetIds.size() < 2
                && gd.interaction.permanentChoiceContext() instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger) {
            harness.handlePermanentChosen(player1, player1.getId());
        }
        harness.passBothPriorities();
    }

    private void declareAttack(Permanent creature) {
        creature.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(indexOf(player2, creature)));
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
