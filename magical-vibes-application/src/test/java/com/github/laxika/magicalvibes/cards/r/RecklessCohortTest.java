package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessCohort.class, ExpeditionEnvoy.class})
class RecklessCohortTest extends BaseCardTest {

    private void addReady(Card card, Player player) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    void mustAttackWithoutAnotherAlly() {
        addReady(new RecklessCohort(), player1);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void anotherAllyLetsItStayHome() {
        addReady(new RecklessCohort(), player1);
        addReady(new ExpeditionEnvoy(), player1);
        beginDeclareAttackers();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    void theCohortDoesNotCountAsAnotherAlly() {
        addReady(new RecklessCohort(), player1);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void opponentsAllyDoesNotLetItStayHome() {
        addReady(new RecklessCohort(), player1);
        addReady(new ExpeditionEnvoy(), player2);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }
}
