package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuntiesSnitchTest extends BaseCardTest {

    private Card putSnitchInGraveyard() {
        Card snitch = new AuntiesSnitch();
        gd.playerGraveyards.get(player1.getId()).add(snitch);
        return snitch;
    }

    private void addReadyAttacker(Permanent perm) {
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(perm);
    }

    private void runCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage → triggers onto stack
        harness.passBothPriorities(); // resolve the graveyard trigger (MayEffect prompt)
    }

    @Test
    @DisplayName("A Goblin dealing combat damage lets you return the Snitch from graveyard to hand")
    void goblinCombatDamageReturnsSnitch() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new Permanent(new SkirkProspector()));
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(snitch);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("Declining the may ability leaves the Snitch in the graveyard")
    void decliningLeavesSnitchInGraveyard() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new Permanent(new SkirkProspector()));
        harness.setLife(player2, 20);

        runCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("A creature that is neither Goblin nor Rogue does not trigger the Snitch")
    void nonGoblinRogueDoesNotTrigger() {
        Card snitch = putSnitchInGraveyard();
        addReadyAttacker(new Permanent(new GrizzlyBears()));
        harness.setLife(player2, 20);

        runCombatDamage();

        // No may ability was queued; the Snitch stays in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(snitch);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snitch);
    }

    @Test
    @DisplayName("Auntie's Snitch cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent snitch = new Permanent(new AuntiesSnitch());
        snitch.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(snitch);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }
}
