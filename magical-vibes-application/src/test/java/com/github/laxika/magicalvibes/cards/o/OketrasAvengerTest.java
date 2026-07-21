package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OketrasAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Exerting prevents all combat damage that would be dealt to Oketra's Avenger")
    void exertPreventsCombatDamageToAvenger() {
        // 3/1 attacks and exerts; a 2/2 blocker's 2 combat damage would be lethal but is prevented.
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        blockAndResolveCombat(0, 0);

        // Combat damage to the exerted attacker is prevented, so it survives; the 2/2 still takes 3 and dies.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(avenger);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Declining exert leaves Oketra's Avenger to die to lethal combat damage")
    void decliningExertLeavesAvengerVulnerable() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        blockAndResolveCombat(0, 0);

        // No prevention: the 2/2 deals 2 to the 1-toughness attacker, killing it.
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(avenger);
    }

    @Test
    @DisplayName("Exerting only prevents combat damage — noncombat damage still kills Oketra's Avenger")
    void exertDoesNotPreventNoncombatDamage() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());
        addCreatureReady(player1, new ProdigalSorcerer()); // T: deal 1 damage to any target

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Ping the exerted attacker for 1 noncombat damage — combat-only prevention does not stop it.
        harness.activateAbility(player1, 1, null, avenger.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(avenger);
    }

    @Test
    @DisplayName("Exerting keeps Oketra's Avenger tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent avenger = addCreatureReady(player1, new OketrasAvenger());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(avenger.isTapped()).isTrue();
        assertThat(avenger.getSkipUntapCount()).isGreaterThan(0);
    }

    // ===== Helpers =====

    private void declareAttackers(List<Integer> attackerIndices) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackerIndices);
    }

    private void blockAndResolveCombat(int blockerIndex, int attackerIndex) {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        harness.passBothPriorities();
    }
}
