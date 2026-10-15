package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoringCeratops.class, RaptorCompanion.class})
class GoringCeratopsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Goring Ceratops grants double strike to other creatures you control")
    void attackGrantsDoubleStrikeToOtherCreatures() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);

        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        companion.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0, 1));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(companion.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures you control also gain double strike")
    void nonAttackingOwnCreaturesAlsoGainDoubleStrike() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);

        Permanent stayBack = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        stayBack.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Only ceratops attacks, companion stays back
        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // "Other creatures you control" includes non-attacking ones
        assertThat(stayBack.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do not gain double strike")
    void opponentCreaturesDoNotGainDoubleStrike() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        opponentCreature.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(opponentCreature.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Goring Ceratops")))
                .isTrue();
    }
    @Test
    @DisplayName("Double strike lasts through the end step and expires before the next turn")
    void doubleStrikeExpiresAtEndOfTurn() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        assertThat(companion.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(companion.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trigger affects creatures present at resolution but not later arrivals")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        Permanent earlyArrival = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());
        assertThat(earlyArrival.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();
        Permanent lateArrival = harness.enterBattlefieldAndReturn(player1, new RaptorCompanion());

        assertThat(earlyArrival.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(lateArrival.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger resolves even if Goring Ceratops leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent ceratops = harness.addToBattlefieldAndReturn(player1, new GoringCeratops());
        ceratops.setSummoningSick(false);
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, ceratops));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goring Ceratops");
        assertThat(companion.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
