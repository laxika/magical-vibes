package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
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

class RuricTharTheUnbowedTest extends BaseCardTest {

    private void resolveStack() {
        for (int i = 0; i < 8 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }

    private Permanent addRuricThar(com.github.laxika.magicalvibes.model.Player player) {
        Permanent ruricThar = new Permanent(new RuricTharTheUnbowed());
        ruricThar.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(ruricThar);
        return ruricThar;
    }

    @Test
    @DisplayName("Dealing 6 damage to an opponent who casts a noncreature spell")
    void damagesOpponentCastingNoncreatureSpell() {
        addRuricThar(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castSorcery(player2, 0, 0);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Dealing 6 damage to its own controller when they cast a noncreature spell")
    void damagesOwnControllerCastingNoncreatureSpell() {
        addRuricThar(player1);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 6);
    }

    @Test
    @DisplayName("Not triggering when a creature spell is cast")
    void noDamageWhenCreatureSpellCast() {
        addRuricThar(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Declaring no attackers while Ruric Thar can attack throws exception")
    void mustAttackWhenAble() {
        addRuricThar(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Ruric Thar attacks for 6 and stays untapped thanks to vigilance")
    void attacksForSixAndStaysUntapped() {
        harness.setLife(player2, 20);
        Permanent ruricThar = addRuricThar(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(ruricThar.isTapped()).isFalse();
    }
}
