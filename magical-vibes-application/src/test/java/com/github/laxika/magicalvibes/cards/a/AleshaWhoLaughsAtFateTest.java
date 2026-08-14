package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AleshaWhoLaughsAtFateTest extends BaseCardTest {

    @Test
    void attackingPutsCounterAndRaidReturnsCreatureWithinPower() {
        Permanent alesha = addReadyAlesha();
        Card valid = new CentaurCourser();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(valid, tooExpensive));

        declareAttack();
        harness.passBothPriorities();
        assertThat(alesha.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Centaur Courser");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void raidDoesNotTriggerIfYouDidNotAttack() {
        addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Centaur Courser");
    }

    @Test
    void targetMustStillFitAleshasPowerOnResolution() {
        Permanent alesha = addReadyAlesha();
        Card valid = new CentaurCourser();
        harness.setGraveyard(player1, List.of(valid));

        declareAttack();
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));

        alesha.setPowerModifier(-3);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Centaur Courser");
        harness.assertNotOnBattlefield(player1, "Centaur Courser");
    }

    private Permanent addReadyAlesha() {
        Permanent alesha = new Permanent(new AleshaWhoLaughsAtFate());
        alesha.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(alesha);
        return alesha;
    }

    private void declareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
