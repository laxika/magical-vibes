package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdvocateOfTheBeast;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaraudingMaulhornTest extends BaseCardTest {

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card,
                               com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void beginDeclareAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @DisplayName("Marauding Maulhorn must attack when its controller has no Advocate of the Beast")
    void mustAttackWithoutAdvocate() {
        addReady(new MaraudingMaulhorn(), player1);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Marauding Maulhorn may stay home while its controller controls Advocate of the Beast")
    void notForcedWithAdvocate() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new AdvocateOfTheBeast(), player1);
        beginDeclareAttackers();

        assertThatCode(() -> gs.declareAttackers(gd, player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An opponent's Advocate of the Beast does not free Marauding Maulhorn")
    void opponentAdvocateDoesNotHelp() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new AdvocateOfTheBeast(), player2);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A differently named creature does not free Marauding Maulhorn")
    void otherCreatureDoesNotHelp() {
        addReady(new MaraudingMaulhorn(), player1);
        addReady(new GrizzlyBears(), player1);
        beginDeclareAttackers();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Marauding Maulhorn attacking satisfies the requirement")
    void attackingIsLegal() {
        harness.setLife(player2, 20);
        addReady(new MaraudingMaulhorn(), player1);
        beginDeclareAttackers();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }
}
