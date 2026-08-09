package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplexAutomatonTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself at upkeep when its controller has seven permanents")
    void returnsSelfWithSevenPermanents() {
        ComplexAutomaton automaton = new ComplexAutomaton();
        harness.addToBattlefield(player1, automaton);
        addFillerPermanents(player1, 6);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == automaton);
        assertThat(gd.playerHands.get(player1.getId())).contains(automaton);
    }

    @Test
    @DisplayName("Does not trigger when its controller has fewer than seven permanents")
    void doesNotTriggerWithSixPermanents() {
        ComplexAutomaton automaton = new ComplexAutomaton();
        harness.addToBattlefield(player1, automaton);
        addFillerPermanents(player1, 5);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == automaton);
    }

    @Test
    @DisplayName("Does nothing if its controller has fewer than seven permanents when the trigger resolves")
    void doesNothingIfPermanentCountDropsBeforeResolution() {
        ComplexAutomaton automaton = new ComplexAutomaton();
        harness.addToBattlefield(player1, automaton);
        addFillerPermanents(player1, 6);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getCard() != automaton);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == automaton);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(automaton);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        ComplexAutomaton automaton = new ComplexAutomaton();
        harness.addToBattlefield(player1, automaton);
        addFillerPermanents(player1, 6);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == automaton);
    }

    private void addFillerPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card filler = new Card();
            filler.setName("Filler Permanent");
            filler.setType(CardType.CREATURE);
            filler.setPower(1);
            filler.setToughness(1);
            harness.addToBattlefield(player, filler);
        }
    }
}
