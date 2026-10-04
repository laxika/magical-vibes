package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElderGargaroth.class, GrizzlyBears.class})
class ElderGargarothTest extends BaseCardTest {

    private static final String CREATE_BEAST = "Create a 3/3 green Beast creature token.";
    private static final String GAIN_LIFE = "You gain 3 life.";
    private static final String DRAW_CARD = "Draw a card.";

    @Test
    @DisplayName("Attacking with Elder Gargaroth and choosing the token mode creates a Beast")
    void attackTokenMode() {
        Permanent gargaroth = addReadyGargaroth(player1);

        declareAttackers(gargaroth);
        resolveTriggerAndChoose(CREATE_BEAST);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getCard().getName()))
                .containsExactly("Beast");
    }

    @Test
    @DisplayName("Attacking with Elder Gargaroth and choosing the life mode gains 3 life")
    void attackLifeMode() {
        addReadyGargaroth(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(gd.playerBattlefields.get(player1.getId()).getFirst());
        resolveTriggerAndChoose(GAIN_LIFE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Attacking with Elder Gargaroth and choosing the draw mode draws a card")
    void attackDrawMode() {
        addReadyGargaroth(player1);
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(gd.playerBattlefields.get(player1.getId()).getFirst());
        resolveTriggerAndChoose(DRAW_CARD);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Blocking with Elder Gargaroth triggers the modal ability")
    void blockTrigger() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent gargaroth = addReadyGargaroth(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(gargaroth);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveTriggerAndChoose(player2, GAIN_LIFE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Attack mode is chosen before players can respond")
    void attackModeChosenBeforePriority() {
        Permanent gargaroth = addReadyGargaroth(player1);
        declareAttackers(gargaroth);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, GAIN_LIFE);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Blocking multiple creatures triggers only once")
    void blockingMultipleCreaturesTriggersOnce() {
        Permanent first = addReadyCreature(player1, new GrizzlyBears());
        Permanent second = addReadyCreature(player1, new GrizzlyBears());
        first.setAttacking(true);
        second.setAttacking(true);
        Permanent gargaroth = addReadyGargaroth(player2);
        gargaroth.setAdditionalBlocksUntilEndOfTurn(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(gargaroth);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, gd.playerBattlefields.get(player1.getId()).indexOf(first)),
                new BlockerAssignment(blockerIndex, gd.playerBattlefields.get(player1.getId()).indexOf(second))));
        resolveTriggerAndChoose(player2, GAIN_LIFE);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Blocking and choosing draw gives the defending controller a card")
    void blockDrawMode() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent gargaroth = addReadyGargaroth(player2);
        ElderGargaroth cardToDraw = new ElderGargaroth();
        harness.setLibrary(player2, List.of(cardToDraw));
        int opponentHandBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gargaroth),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveTriggerAndChoose(player2, DRAW_CARD);

        assertThat(gd.playerHands.get(player2.getId())).contains(cardToDraw);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandBefore);
    }

    @Test
    @DisplayName("Blocking and choosing token creates a Beast for the defender")
    void blockTokenMode() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent gargaroth = addReadyGargaroth(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(gargaroth),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveTriggerAndChoose(player2, CREATE_BEAST);

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getCard().getName())).containsExactly("Beast");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private Permanent addReadyGargaroth(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new ElderGargaroth());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void declareAttackers(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }

    private void resolveTriggerAndChoose(String mode) {
        resolveTriggerAndChoose(player1, mode);
    }

    private void resolveTriggerAndChoose(com.github.laxika.magicalvibes.model.Player player, String mode) {
        harness.passBothPriorities();
        harness.handleListChoice(player, mode);
    }
}
