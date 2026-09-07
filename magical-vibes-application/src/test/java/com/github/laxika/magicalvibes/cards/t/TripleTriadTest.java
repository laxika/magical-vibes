package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TripleTriad.class, HillGiant.class, GrizzlyBears.class})
class TripleTriadTest extends BaseCardTest {

    @Test
    @DisplayName("Lets the controller play their own card and another exiled card with lesser mana value for free")
    void grantsFreePlayForOwnAndLesserManaValueCards() {
        harness.addToBattlefield(player1, new TripleTriad());
        CardRef cards = setTopCards(new HillGiant(), new GrizzlyBears());

        resolveTripleTriadTrigger();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, cards.opponentCard().getId());
        harness.passBothPriorities();
        harness.castFromExile(player1, cards.ownCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Does not allow an exiled card with equal mana value")
    void rejectsEqualManaValueCard() {
        harness.addToBattlefield(player1, new TripleTriad());
        CardRef cards = setTopCards(new HillGiant(), new HillGiant());

        resolveTripleTriadTrigger();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromExile(player1, cards.opponentCard().getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permission to play this exiled card");
    }

    private CardRef setTopCards(Card ownCard, Card opponentCard) {
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        return new CardRef(ownCard, opponentCard);
    }

    private void resolveTripleTriadTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    private record CardRef(Card ownCard, Card opponentCard) {
    }
}
