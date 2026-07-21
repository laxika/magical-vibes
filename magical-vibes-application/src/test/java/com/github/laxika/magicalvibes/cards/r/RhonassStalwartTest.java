package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RhonassStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives +1/+1 until end of turn")
    void exertBoosts() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent stalwart = addReadyStalwart(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(stalwart.isTapped()).isTrue();
        assertThat(stalwart.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("After exerting, can't be blocked by a creature with power 2 or less")
    void exertCannotBeBlockedByLowPower() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("After exerting, can be blocked by a creature with power 3 or greater")
    void exertCanBeBlockedByHighPower() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(giant);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(giant.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining exert leaves base stats and allows low-power blockers")
    void decliningExertDoesNothing() {
        Permanent stalwart = addReadyStalwart(player1);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalwart)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalwart)).isEqualTo(2);
        assertThat(stalwart.getSkipUntapCount()).isZero();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(stalwart);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(bears.isBlocking()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyStalwart(Player player) {
        return addCreatureReady(player, new RhonassStalwart());
    }

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
