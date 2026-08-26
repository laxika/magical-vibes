package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeysaOpulentOligarch.class, NoviceInspector.class, Shock.class})
class TeysaOpulentOligarchTest extends BaseCardTest {

    @Test
    void investigatesForEachOpponentWhoLostLifeThisTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        dealDamageToOpponent();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenNoOpponentLostLifeThisTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void createsOnlyOneSpiritWhenMultipleCluesArePutIntoGraveyardInOneTurn() {
        harness.addToBattlefield(player1, new TeysaOpulentOligarch());
        castNoviceInspector();
        dealDamageToOpponent();
        advanceToEndStep();

        List<Permanent> clues = List.copyOf(findPermanents(player1, "Clue"));
        assertThat(clues).hasSize(2);
        for (Permanent clue : clues) {
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
            harness.sacrificePermanent(player1, index, clue.getId());
            harness.passBothPriorities();
        }

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    private void castNoviceInspector() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void dealDamageToOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
