package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LibraryLarcenist.class, LlanowarVisionary.class, Shock.class})
class LibraryLarcenistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Library Larcenist draws a card")
    void attackingDrawsCard() {
        harness.setLibrary(player1, List.of(new LlanowarVisionary()));
        Permanent larcenist = addCreatureReady(player1, new LibraryLarcenist());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(larcenist)));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Visionary");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each attacking Library Larcenist draws exactly one card")
    void multipleLarcenistsEachDrawOneCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new LlanowarVisionary(), new Shock(), new LlanowarVisionary()));
        addCreatureReady(player1, new LibraryLarcenist());
        addCreatureReady(player1, new LibraryLarcenist());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Llanowar Visionary");
        harness.assertInHand(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Library Larcenist does not draw when only another creature attacks")
    void anotherCreatureAttackingDoesNotTriggerDraw() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        addCreatureReady(player1, new LibraryLarcenist());
        addCreatureReady(player1, new LlanowarVisionary());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opposing Library Larcenist draws for its controller")
    void opponentAttackingDrawsForOpponent() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(new LlanowarVisionary()));
        addCreatureReady(player2, new LibraryLarcenist());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Llanowar Visionary");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The attack trigger still draws if Library Larcenist dies in response")
    void removingAttackerDoesNotStopDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new LlanowarVisionary()));
        Permanent larcenist = addCreatureReady(player1, new LibraryLarcenist());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.castInstant(player2, 0, larcenist.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Library Larcenist");
        harness.assertNotOnBattlefield(player1, "Library Larcenist");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Visionary");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
