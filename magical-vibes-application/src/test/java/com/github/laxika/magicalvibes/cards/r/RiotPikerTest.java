package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiotPikerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Riot Piker puts it on the battlefield")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new RiotPiker()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Riot Piker");
    }

    @Test
    @DisplayName("Declaring no attackers while Riot Piker can attack throws exception")
    void mustAttackWhenAble() {
        Permanent piker = new Permanent(new RiotPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Omitting Riot Piker from attackers while declaring other creatures throws exception")
    void mustBeIncludedAmongAttackers() {
        Permanent piker = new Permanent(new RiotPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piker);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Riot Piker does not need to attack with summoning sickness")
    void doesNotAttackWithSummoningSickness() {
        harness.setLife(player2, 20);

        Permanent piker = new Permanent(new RiotPiker());
        gd.playerBattlefields.get(player1.getId()).add(piker);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Riot Piker deals 2 combat damage when unblocked")
    void dealsTwoDamageUnblocked() {
        harness.setLife(player2, 20);

        Permanent piker = new Permanent(new RiotPiker());
        piker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
