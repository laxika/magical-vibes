package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReadyGargaroth(com.github.laxika.magicalvibes.model.Player player) {
        return addReadyCreature(player, new ElderGargaroth());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
