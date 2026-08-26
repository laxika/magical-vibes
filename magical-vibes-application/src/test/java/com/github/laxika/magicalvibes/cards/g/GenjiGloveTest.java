package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenjiGlove.class, GrizzlyBears.class})
class GenjiGloveTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking in the first combat phase untaps the creature and grants another combat phase")
    void firstCombatAttackUntapsAndGrantsExtraCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 1);
        assertThat(creature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking in a later combat phase does not untap or grant another combat phase")
    void laterCombatAttackDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent glove = addGloveReady(player1);
        glove.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0), 2);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    private Permanent addGloveReady(Player player) {
        Permanent glove = new Permanent(new GenjiGlove());
        glove.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(glove);
        return glove;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
