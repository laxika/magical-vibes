package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LordOfChange.class, SwordsToPlowshares.class})
class LordOfChangeTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it enters")
    void drawsThreeCardsOnEntry() {
        harness.setLibrary(player1, List.of(new SwordsToPlowshares(), new SwordsToPlowshares(), new SwordsToPlowshares()));
        harness.setHand(player1, List.of(new LordOfChange()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Entering without being cast draws three cards for its controller")
    void drawsThreeCardsWithoutBeingCast() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SwordsToPlowshares(), new SwordsToPlowshares(),
                new SwordsToPlowshares(), new SwordsToPlowshares()));
        int opponentHandSize = gd.playerHands.get(player1.getId()).size();

        harness.enterBattlefieldAndReturn(player2, new LordOfChange());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(opponentHandSize);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when payment is declined")
    void decliningWardCountersOpponentsSpell() {
        Permanent lord = harness.addToBattlefieldAndReturn(player2, new LordOfChange());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, lord.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Lord of Change");
        harness.assertInGraveyard(player1, "Swords to Plowshares");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Paying three mana for ward allows the opponent's spell to resolve")
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent lord = harness.addToBattlefieldAndReturn(player2, new LordOfChange());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, lord.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Lord of Change");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertLife(player2, 26);
    }

    @Test
    @DisplayName("The controller's own spell does not trigger ward")
    void ownSpellDoesNotTriggerWard() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfChange());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, lord.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lord of Change");
        harness.assertLife(player1, 26);
    }
}
