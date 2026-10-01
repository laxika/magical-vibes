package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Calciderm;
import com.github.laxika.magicalvibes.cards.s.SpellbreakerBehemoth;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DismalFailure.class, Calciderm.class, SpellbreakerBehemoth.class})
class DismalFailureTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and its controller discards a card")
    void countersSpellAndMakesItsControllerDiscard() {
        Calciderm calciderm = new Calciderm();
        DismalFailure cardToDiscard = new DismalFailure();
        harness.setHand(player1, new ArrayList<>(List.of(calciderm, cardToDiscard)));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, calciderm.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Calciderm");
        harness.assertNotOnBattlefield(player1, "Calciderm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still makes the controller discard when the target spell cannot be countered")
    void discardsFromUncounterableSpellController() {
        harness.addToBattlefield(player1, new SpellbreakerBehemoth());
        Calciderm calciderm = new Calciderm();
        DismalFailure cardToDiscard = new DismalFailure();
        harness.setHand(player1, new ArrayList<>(List.of(calciderm, cardToDiscard)));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, calciderm.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Calciderm");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Calciderm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Counters the target spell when its controller has no card to discard")
    void countersSpellWhenControllerHasNoCardToDiscard() {
        Calciderm calciderm = new Calciderm();
        harness.setHand(player1, List.of(calciderm));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, calciderm.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Calciderm");
        harness.assertNotOnBattlefield(player1, "Calciderm");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fizzles without a discard if the target spell leaves the stack")
    void fizzlesIfTargetSpellLeavesStack() {
        Calciderm calciderm = new Calciderm();
        DismalFailure cardToKeep = new DismalFailure();
        harness.setHand(player1, new ArrayList<>(List.of(calciderm, cardToKeep)));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.setHand(player2, List.of(new DismalFailure()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, calciderm.getId());
        gd.stack.removeIf(entry -> entry.getCard().getName().equals("Calciderm"));

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToKeep);
    }
}
