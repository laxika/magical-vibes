package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaiyuuStormsEdge.class, ElvishWarrior.class, GrizzlyBears.class, MothriderSamurai.class})
class RaiyuuStormsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai attacking alone in the first combat phase is untapped and grants an extra combat")
    void samuraiAttackingAloneFirstCombatUntapsAndGrantsExtraCombat() {
        harness.addToBattlefield(player1, new RaiyuuStormsEdge());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(player1, List.of(1), 1);
        assertThat(samurai.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(samurai.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A Warrior attacking alone in the first combat phase is untapped and grants an extra combat")
    void warriorAttackingAloneFirstCombatUntapsAndGrantsExtraCombat() {
        harness.addToBattlefield(player1, new RaiyuuStormsEdge());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());

        declareAttackers(player1, List.of(1), 1);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("A Samurai or Warrior attacking alone in a later combat is untapped without an extra combat")
    void qualifyingCreatureAttackingAloneLaterCombatOnlyUntaps() {
        harness.addToBattlefield(player1, new RaiyuuStormsEdge());
        Permanent samurai = addCreatureReady(player1, new MothriderSamurai());

        declareAttackers(player1, List.of(1), 2);
        harness.passBothPriorities();

        assertThat(samurai.isTapped()).isFalse();
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature of another type attacking alone does not trigger Raiyuu")
    void otherCreatureAttackingAloneDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaiyuuStormsEdge());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1), 1);

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Raiyuu, Storm's Edge"));
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Samurai or Warrior attacking with another creature does not trigger Raiyuu")
    void multipleAttackersDoNotTrigger() {
        harness.addToBattlefield(player1, new RaiyuuStormsEdge());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2), 1);

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Raiyuu, Storm's Edge"));
        assertThat(warrior.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
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
