package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredatoryAdvantageTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    private long lizardTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Lizard"))
                .count();
    }

    @Test
    @DisplayName("Creates a Lizard on an opponent's end step when they didn't cast a creature spell")
    void createsTokenOnOpponentEndStepWithoutCreatureSpell() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getName().equals("Lizard")
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.LIZARD)
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
        assertThat(lizardTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("No token when the end-step opponent cast a creature spell this turn")
    void noTokenWhenOpponentCastCreatureSpell() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell cast by the opponent still yields a token")
    void noncreatureSpellStillCreatesToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        gd.recordSpellCast(player2.getId(), new Shock());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on the controller's own end step")
    void noTokenOnControllersOwnEndStep() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());

        advanceToEndStepTrigger(player1);

        assertThat(lizardTokens(player1)).isZero();
    }
}
