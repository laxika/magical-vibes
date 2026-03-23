package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoringCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Goring Ceratops grants double strike to other creatures you control")
    void attackGrantsDoubleStrikeToOtherCreatures() {
        Permanent ceratops = new Permanent(new GoringCeratops());
        ceratops.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ceratops);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures you control also gain double strike")
    void nonAttackingOwnCreaturesAlsoGainDoubleStrike() {
        Permanent ceratops = new Permanent(new GoringCeratops());
        ceratops.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ceratops);

        Permanent stayBack = new Permanent(new GrizzlyBears());
        stayBack.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(stayBack);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Only ceratops attacks, bears stays back
        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // "Other creatures you control" includes non-attacking ones
        assertThat(stayBack.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do not gain double strike")
    void opponentCreaturesDoNotGainDoubleStrike() {
        Permanent ceratops = new Permanent(new GoringCeratops());
        ceratops.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ceratops);

        Permanent opponentCreature = new Permanent(new GrizzlyBears());
        opponentCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(opponentCreature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        Permanent ceratops = new Permanent(new GoringCeratops());
        ceratops.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(ceratops);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Goring Ceratops")))
                .isTrue();
    }
}
